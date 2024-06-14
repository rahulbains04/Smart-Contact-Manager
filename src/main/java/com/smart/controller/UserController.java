package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to the response
	@ModelAttribute 
	public void addCommonData(Model model,Principal principal)
	{
		String userName=principal.getName();
		System.out.println("Username is "+userName);
		
		//getting the user from the username
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("User: "+user);
		
		model.addAttribute("user",user);
	}
	
	
	//dashboard home
	@GetMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
	
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add contact");
		model.addAttribute("contact",new Contact());
	return "normal/add_contact";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session)
	{
		try {
					
			
		String name = principal.getName();
		User user = userRepository.getUserByUserName(name);
		
		//processing and uploading file....
		if(file.isEmpty())
		{
			//if the file is empty then display the message
			System.out.println("file is empty ");
			
		}
		else
		{
			//update the file to folder and update the name to the contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("/static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image has been uploaded");
			
		}
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		System.out.println("Data "+contact);
		System.out.println("Added to database");
		
		//success message
		session.setAttribute("message", new Message("Your contact is added, add more!","success"));
		}
		
		catch(Exception e)
		{
			System.out.println("ERROR: "+e.getMessage());
			e.printStackTrace();
			//error message
			session.setAttribute("message", new Message("Kuch toh gadbad hai Daya,yeh darwaza tod do!","danger"));
		}
		
		return "normal/add_contact";
		
	}
	
	//show contacts handler
	@GetMapping("/show-contacts")
	public String showContacts(Model m,Principal principal)
	{	
		m.addAttribute("title","Show user contacts");
		
		//contact list yield
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
	
		List<Contact> contacts = contactRepository.findContactsByUser(user.getId());
		
		m.addAttribute("contacts",contacts);
		
		
		return "normal/show_contacts";
	}
	
	
	
}
