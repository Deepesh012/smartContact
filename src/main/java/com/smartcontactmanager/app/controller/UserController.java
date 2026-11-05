package com.smartcontactmanager.app.controller;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartcontactmanager.app.dao.ContactRepository;
import com.smartcontactmanager.app.dao.UserRepository;
import com.smartcontactmanager.app.entities.Contact;
import com.smartcontactmanager.app.entities.User;
import com.smartcontactmanager.app.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		// get the user by using username(Email)
		String name = principal.getName();
		User user = userRepository.getUserByUserName(name);
		System.out.println("USER :" + user);
		System.out.println("USERNAME :" + name);
		m.addAttribute("user", user);
	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
		// Get current logged-in user's email
	    String username = principal.getName();

	 // Get user from DB
	    User user = userRepository.findByEmail(username);
	    
	 // Get count of that user's contacts
	    int contactCount = contactRepository.countByUser(user);
	    
	 // Get total user count (admin may want to see this)
	    long userCount = userRepository.count();
	    
	    model.addAttribute("totalUsers", userCount);
	    model.addAttribute("totalContacts", contactCount);
	    model.addAttribute("user", user);
	    
	    int userContactCount = contactRepository.countByUser(user);
	    long totalContactCount = contactRepository.count();  // count of all contacts

	    model.addAttribute("user", user);
	    model.addAttribute("userContactCount", userContactCount);
	    model.addAttribute("totalContactCount", totalContactCount);
	    
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	// open add form handler
	public String openAddContactForm(Model model, HttpSession session) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		 

		return "normal/add_contact_form";
	}

//	processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// processing and uploading file
			if (file.isEmpty()) {
				// if the file is empty then try our message
				System.out.println("File is Empty");
				contact.setImage("contact.png");
			} else {
				// upload the file to the folder and update contact
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is Uploaded");
			}


			contact.setUser(user);
			user.getContacts().add(contact);

			this.userRepository.save(user);

			System.out.println("DATA " + contact);

			System.out.println("Added to Database");
			// message success
			redirectAttributes.addFlashAttribute("message", new Message("Your Contact is added !! Add more..", "success"));

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			
			// message error
			redirectAttributes.addFlashAttribute("message", new Message("Something went wrong !! Try again..", "danger"));
			

		}

		return "redirect:/user/add-contact";
	}

	//show contacts handler
	//per page = 5 contacts
	//current page = 0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title","Show User Contacts");
		//sending contacts list
//		String userName = principal.getName();
//		User user = this.userRepository.getUserByUserName(userName);
//		List<Contact> contacts = user.getContacts();
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);	
		
		//currentPage - page
		//Contact per page - 5
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details.
	@RequestMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	@Transactional
	public String deleteContact(@PathVariable("cid") Integer cID, RedirectAttributes redirectAttributes, Principal principal) {
	    Optional<Contact> contactOptional = this.contactRepository.findById(cID);
	    Contact contact = contactOptional.get();
	    //contact.setUser(null);
	    //this.contactRepository.delete(contact);
	    
	    User user = this.userRepository.getUserByUserName(principal.getName());
	    user.getContacts().remove(contact);
	    
	    this.userRepository.save(user);

	    redirectAttributes.addFlashAttribute("message", new Message("Contact deleted Successfully...", "success"));
	    return "redirect:/user/show-contacts/0";
	}

	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Model m,HttpSession session ,Principal principal, RedirectAttributes redirectAttributes) {
		
		try {
			//old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			//image..
			if(!file.isEmpty()) {
				//file work..
				//rewrite
				
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				
				//update new photo

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());

				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			 redirectAttributes.addFlashAttribute("message", new Message("Contact updated Successfully...", "success"));
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		System.out.println("CONTACT NAME :"+contact.getName());
		System.out.println("CONTACT NAME :"+contact.getcId());
		return "redirect:/user/contact/"+contact.getcId();
	}
	
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title","Profile Page");
		
		return "normal/profile";
	}
	
	
	//open update form handler
		@PostMapping("/update")
		public String updateProfile(Model m) {
			m.addAttribute("title", "Update Profile");
			
			return "normal/profile-update";
		}
		
		
	//update contact handler
//	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
//	public String updateProfileHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Model m,HttpSession session ,Principal principal, RedirectAttributes redirectAttributes) {
//			
//			try {
//				//old contact details
//				Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
//				
//				//image..
//				if(!file.isEmpty()) {
//					//file work..
//					//rewrite
//					
//					//delete old photo
//					File deleteFile = new ClassPathResource("static/img").getFile();
//					File file1 = new File(deleteFile, oldContactDetail.getImage());
//					file1.delete();
//					
//					//update new photo
//
//					File saveFile = new ClassPathResource("static/img").getFile();
//
//					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
//
//					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//					
//					contact.setImage(file.getOriginalFilename());
//
//					
//				}else {
//					contact.setImage(oldContactDetail.getImage());
//				}
//				
//				User user = this.userRepository.getUserByUserName(principal.getName());
//				contact.setUser(user);
//				this.contactRepository.save(contact);
//				
//				 redirectAttributes.addFlashAttribute("message", new Message("Contact updated Successfully...", "success"));
//				
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//			}
//			
//			System.out.println("CONTACT NAME :"+contact.getName());
//			System.out.println("CONTACT NAME :"+contact.getcId());
//			return "redirect:/user/contact/"+contact.getcId();
//		}
		
	



		
	
	
	
}
