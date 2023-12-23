package com.shopme.admin.user;

import java.util.List;
import java.util.NoSuchElementException;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@Service
@Transactional
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public List<User> getAllUsers() {
		return (List<User>) userRepository.findAll();
	}

	public Page<User> getUsersByPage(int pageNumber, int pageSize,
									 String sortField, String sortOrder, String keyword) {
		Sort sort = Sort.by(sortField);
		sort = sortOrder.equals("asc") ? sort.ascending() : sort.descending();

		Pageable pageable = PageRequest.of(pageNumber-1, pageSize, sort);
		if (keyword != null) {
			return userRepository.searchByKeyword(keyword, pageable);
		}
		return userRepository.findAll(pageable);
	}
	
	public List<Role> getAllRoles() {
		return (List<Role>) roleRepository.findAll();
	}
	
	public User saveUser(User user) {
		boolean isUpdatingUser = (user.getId() != null);
		if (isUpdatingUser) {
			// edit mode
			User existingUser = userRepository.findById(user.getId()).get();
			if (user.getPassword().isEmpty()) {
				user.setPassword(existingUser.getPassword());
			} else {
				encodePassword(user);
			}
		} else {
			// new mode
			encodePassword(user);
		}
		return userRepository.save(user);
	}

	private void encodePassword(User user) {
		String encodedPwd = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPwd);
	}

	public boolean isUniqueEmail(Integer id, String email) {
		User user = userRepository.getByEmail(email);
		if (id == null) {
			// new mode
			return user == null;
		} else {
			// edit mode
			if (user == null) {
				return true;
			}
			else {
				if (user.getId() == id)	return true;
				return false;
			}
		}
	}

	public User getUser(Integer id) throws UserNotFoundException {
		try {
			return userRepository.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new UserNotFoundException("Could not find user " + id);
		}
	}

	public void deleteUser(Integer id) throws UserNotFoundException {
		Long countById = userRepository.countById(id);
		if (countById == null || countById == 0) {
			throw new UserNotFoundException("Could not find user " + id);
		}
		userRepository.deleteById(id);
	}

	public void updateUserEnabledStatus(Integer id, boolean enabled) {
		userRepository.updateEnabledStatus(id, enabled);
	}

}