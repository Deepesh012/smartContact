package com.smartcontactmanager.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontactmanager.app.dao.UserRepository;
import com.smartcontactmanager.app.entities.User;
import com.smartcontactmanager.app.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	

	    @RequestMapping("/signin")
	    public String loginPage(Model model) {
	        model.addAttribute("title", "Login - Smart Contact Manager");
	        return "login";  // This should match login.html in templates
	    }


	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	


	// this handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult bresult,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,HttpSession session) {
		
		try {
			if(!agreement) {
				System.out.println("You have not agreed the terms and conditions..!");
				throw new Exception("You have not agreed the terms and conditions..!");
			}
			
			if (bresult.hasErrors()) {
				System.out.println("ERROR :"+bresult.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement :" +agreement);
			System.out.println("USER :" +user);
			User user2 = this.userRepository.save(user);
			
			
			model.addAttribute("user",new User());
			
			session.setAttribute("message", new Message("Successfully Register !!","alert-success"));
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went Wrong !!"+e.getMessage(),"alert-danger"));
			return "signup";
		}

	}
}
