package com.shopme.admin.user;

import java.io.IOException;
import java.util.List;

import com.shopme.admin.utils.FileUploadUtil;
import com.shopme.common.constant.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@Controller
public class UserController {

	@Autowired
	private UserService userService;
	
	@GetMapping("/users")
	public String listFirstPage(Model model) {
		return listByPage(1, model, "firstName", "asc", null);
	}

	@GetMapping("/users/page/{pageNum}")
	public String listByPage(@PathVariable("pageNum") int pageNum,
							 Model model,
							 @Param("sortField") String sortField,
							 @Param("sortOrder") String sortOrder,
							 @Param("keyword") String keyword) {
		System.out.println("Sort field: " + sortField);
		System.out.println("Sort order: " + sortOrder);
		Page<User> page = userService.getUsersByPage(pageNum, Constant.USER_PAGE_SIZE, sortField, sortOrder, keyword);
		List<User> listUsers = page.getContent();

		model.addAttribute("listUsers", listUsers);
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalElements", page.getTotalElements());

		long startCount = (pageNum - 1) * Constant.USER_PAGE_SIZE + 1;
		long endCount = startCount + Constant.USER_PAGE_SIZE - 1;
		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortOrder", sortOrder);
		model.addAttribute("keyword", keyword);
		model.addAttribute("reverseSortOrder", sortOrder.equals("asc") ? "desc" : "asc");

		return "users";
	}
	
	@GetMapping("/users/new")
	public String newUser(Model model) {
		User user = new User();
		user.setEnabled(true);
		model.addAttribute("user", user);
		model.addAttribute("pageTitle", "Create New User");
		
		List<Role> roles = userService.getAllRoles();
		model.addAttribute("listRoles", roles);
		return "user_form";
	}
	
	@PostMapping("/users/save")
	public String saveUser(User user, RedirectAttributes redirectAttributes,
						   @RequestParam("image")MultipartFile multipartFile) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			user.setPhotos(fileName);
			User savedUser = userService.saveUser(user);

			String uploadDir = Constant.USER_PHOTO_DIR + "/" + savedUser.getId();
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			userService.saveUser(user);
		}
		redirectAttributes.addFlashAttribute("message", "The user has been saved successfully.");
		return "redirect:/users";
	}

	@GetMapping("/users/edit/{id}")
	public String editUser(@PathVariable(name = "id") Integer id,
						   Model model,
						   RedirectAttributes redirectAttributes) {
		try {
			User user = userService.getUser(id);
			model.addAttribute("user", user);
			model.addAttribute("pageTitle", "Edit User (ID: " + id + ")");

			List<Role> roles = userService.getAllRoles();
			model.addAttribute("listRoles", roles);

			return "user_form";
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
		}
		return "redirect:/users";
	}

	@GetMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable("id") Integer id,
							 RedirectAttributes redirectAttributes) {
		try {
			userService.deleteUser(id);
			redirectAttributes.addFlashAttribute("message",
					"The user " + id + " has been deleted successfully");
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
		}
		return "redirect:/users";
	}

	@GetMapping("/users/{id}/enabled/{status}")
	public String updateUserEnabledStatus(@PathVariable("id") Integer id,
										  @PathVariable("status") boolean status,
										  RedirectAttributes redirectAttributes) {
		userService.updateUserEnabledStatus(id, status);
		String message = "The user " + id + " has been " + (status?"enabled":"disabled");
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/users";
	}

}
