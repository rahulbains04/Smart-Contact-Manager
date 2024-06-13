package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
//	
//	@GetMapping("/test")
//	@ResponseBody
//	public String test() {
//		User user = new User();
//		user.setName("Pushpa Raj");
//		user.setEmail("nhijhukega@sala.io");
//		Contact contact = new Contact();
//		contact.setName("Srivalli");
//		contact.setWork("Doodhwali");
//		user.getContacts().add(contact);
//		userRepository.save(user);
//		return "Working";
//	}
	
	@GetMapping("/")
	public String home(Model m)
	{
		m.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model m)
	{
		m.addAttribute("title","About us");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model m)
	{
		m.addAttribute("title","Register - Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,@RequestParam(value="agreement",defaultValue="false")boolean agreement,Model model,
			BindingResult result1,HttpSession session)
	{
		try {
			if(!agreement)
			{
				System.out.println("Terms and conditions are not agreed upon");
				throw new Exception("Terms and conditions are not agreed upon");
			}
			
			if(result1.hasErrors())
			{
				System.out.println("ERROR here is "+result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			User result = this.userRepository.save(user);
			
			System.out.println("agreement "+ agreement);
		System.out.println("User "+result);
		
		model.addAttribute("user",new User());
		session.setAttribute("message",new Message("Successfully registered","alert-success"));
		return "signup";
		
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("Something went wrong "+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
		
	}
	
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title","Login Page");
		return "login";
	}
	
	@GetMapping("/login-fail")
	public String loginFail(Model model)
	{
		model.addAttribute("title","Login fail");
		return "login-fail";
	}
	
	
}
