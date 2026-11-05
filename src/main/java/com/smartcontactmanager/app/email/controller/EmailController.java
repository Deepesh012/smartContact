package com.smartcontactmanager.app.email.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.smartcontactmanager.app.email.model.EmailRequest;
import com.smartcontactmanager.app.email.service.EmailService;

@RestController
public class EmailController {

	@Autowired
	private EmailService emailService;
	
	
	@RequestMapping("/welcome")
	public String welcome()
	{
		return "hello..! this is my email api";
	}
	
	// GET request -> open form page
    @GetMapping("/sendemail")
    public String sendEmailPage() {
        return "Email"; // this should match sendemail.html in templates folder
    }

	
	//api to send email
	@RequestMapping(value = "/sendemail", method = RequestMethod.POST)
	public ResponseEntity<?> sendEmail(@RequestBody EmailRequest request)
	{
		
		System.out.println(request);
		boolean check = this.emailService.sendEmail(request.getSubject(), request.getMessage(), request.getTo());
		
		if(check)
		{
			return ResponseEntity.ok("EMAIL is sent successfully");
		}
		else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Email not sent..");
		}
		
	}
	
}
