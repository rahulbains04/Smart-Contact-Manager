package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
			contact.setImage("contact.png");
			
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
	//per page=5[n]
	//current page=0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") int page,Model m,Principal principal)
	{	
		m.addAttribute("title","Show user contacts");
		
		//contact list yield
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		// pageable will take 2 details contacts per page and the page no
		Pageable pageable = PageRequest.of(page,3);
	
		Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	
	//showing specific contact details
	
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId")int cId,Model model,Principal principal)
	{
		System.out.println("cId "+cId);
		
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		
	
		
		return "normal/contact_detail";
	}
	
	// delete contact handler
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") int cId,Model model,Principal principal,HttpSession session)
	{
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String useName = principal.getName();
		User user = userRepository.getUserByUserName(useName);
		
		if(user.getId()==contact.getUser().getId())
		{
			contact.setUser(null);
			contactRepository.delete(contact);
			session.setAttribute("message", new Message("contact deleted successfully..","success"));
		}
		
		
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	
}
