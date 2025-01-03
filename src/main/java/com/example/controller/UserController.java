package com.example.controller;

import com.example.model.Owners;
import com.example.model.SlotDetails;
import com.example.model.Users;
import com.example.repository.OwnersRepository;
import com.example.repository.SlotDetailsRepository;
import com.example.repository.UsersRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private OwnersRepository ownersRepository;

    @Autowired
    private SlotDetailsRepository slotDetailsRepository;

    // Landing page route
    @GetMapping("/")
    public String showLandingPage() {
        return "land";
    }

    // User login page route
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Display user registration page
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new Users());
        return "register";
    }

    // Handle user registration
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") Users user, Model model) {
        if (usersRepository.findByEmail(user.getEmail()) != null) {
            model.addAttribute("status", "Email is already registered!");
            return "register";
        }

        usersRepository.save(user);
        model.addAttribute("status", "Registration successful! You can now log in.");
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("user") Users loginUser, Model model, HttpSession session) {
        Users userFromDb = usersRepository.findByUsername(loginUser.getUsername());

        if (userFromDb != null && userFromDb.getPassword().equals(loginUser.getPassword())) {
            session.setAttribute("loggedInEmail", userFromDb.getEmail());
            model.addAttribute("status", "Login successful!");
            return "index";
        } else {
            model.addAttribute("status", "Invalid credentials. Please try again.");
            return "login";
        }
    }

    // Display owner registration page
    @GetMapping("/ownregister")
    public String showOwnerRegistrationForm(Model model) {
        model.addAttribute("owner", new Owners());
        return "ownerreg";
    }

    // Handle owner registration
    @PostMapping("/ownregister")
public String registerOwner(@ModelAttribute("owner") Owners owner,  
                            Model model) {

    // Validate organization type
    List<String> validOrgTypes = Arrays.asList("mall", "restaurant", "hospital");
    if (!validOrgTypes.contains(owner.getOrgType().toLowerCase())) {
        model.addAttribute("status", "Invalid organization type!");
        return "ownregister";
    }

    // Check if email already exists
    if (ownersRepository.findByEmail(owner.getEmail()) != null) {
        model.addAttribute("status", "Email is already registered!");
        return "ownregister";
    }

    // Validate passwords
    if (owner.getPassword().length() < 8) {
        model.addAttribute("status", "Password must be at least 8 characters long!");
        return "ownregister";
    }

    // Save owner to the database
    ownersRepository.save(owner);

    // Redirect with a success message
    model.addAttribute("status", "Owner registration successful! Please log in.");
    return "redirect:/ownerlog";
}



    // Display owner login page
    @GetMapping("/ownerlog")
    public String showOwnerLoginPage() {
        return "ownerlogin";
    }

    // Handle owner login
    @PostMapping("/ownerlog")
    public String loginOwner(@ModelAttribute("owner") Owners owner, Model model, HttpSession session) {
        Owners existingOwner = ownersRepository.findByEmail(owner.getEmail());

        if (existingOwner != null && existingOwner.getPassword().equals(owner.getPassword())) {
            session.setAttribute("loggedInEmail", existingOwner.getEmail());
            return "redirect:/ownerdashboard";
        } else {
            model.addAttribute("status", "Invalid email or password!");
            return "ownerlogin";
        }
    }

    // Owner dashboard page route
    @GetMapping("/ownerdashboard")
    public String showOwnerDashboard(HttpSession session, Model model) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        if (loggedInEmail == null) {
            return "redirect:/ownerlog"; // Redirect to login if no session is found
        }
        model.addAttribute("email", loggedInEmail);
        return "ownerhome";
    }

    @GetMapping("/addslot")
    public String showaddslot(HttpSession session, Model model) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail"); // Use correct key
        if (loggedInEmail == null) {
            return "redirect:/ownerlog"; // Redirect to login if no session is found
        }
        model.addAttribute("email", loggedInEmail); // Optionally pass the email to the view
        return "addslot";
    }


    @PostMapping("/addSlotDetails")
public String addSlotDetails(@RequestParam("slotNumber") String slotNumber,
                              @RequestParam("floorNumber") String floorNumber,
                              HttpSession session, 
                              Model model) {
    // Retrieve the owner's email from the session
    String ownerEmail = (String) session.getAttribute("loggedInEmail");
    if (ownerEmail == null) {
        return "redirect:/ownerlog"; // Redirect to login if no session is found
    }

    // Validate input fields
    if (slotNumber.isEmpty() || floorNumber.isEmpty()) {
        model.addAttribute("status", "Slot number and floor number cannot be empty!");
        return "addslot";
    }

    // Create a new SlotDetails object
    SlotDetails slotDetails = new SlotDetails();
    slotDetails.setOwnerEmail(ownerEmail);
    slotDetails.setSlotNumber(slotNumber);
    slotDetails.setFloorNumber(floorNumber);
    slotDetails.setBookingDate(null); // Initially null
    slotDetails.setCheckinTime(null); // Initially null
    slotDetails.setStatus(null); // Initially null

    // Save to the database
    slotDetailsRepository.save(slotDetails);

    // Redirect to dashboard with success message
    model.addAttribute("status", "Slot added successfully!");
    return "redirect:/ownerdashboard";
}
@GetMapping("/ownerslots")
public String getOwnerSlots(HttpSession session, Model model) {
    // Retrieve the owner's email from the session
    String ownerEmail = (String) session.getAttribute("loggedInEmail");
    if (ownerEmail == null) {
        return "redirect:/ownerlog"; // Redirect to login if no session is found
    }

    // Fetch all slots associated with the owner
    List<SlotDetails> slots = slotDetailsRepository.findByOwnerEmail(ownerEmail);

    // Add the slots to the model
    model.addAttribute("slots", slots);

    // Return the view to display the slots
    return "ownerslot"; // Ensure this matches the HTML file name
}


@GetMapping("/owners")
public String getAllOwners(Model model, HttpSession session) {
    // Check if the user is logged in
    String loggedInEmail = (String) session.getAttribute("loggedInEmail");
    if (loggedInEmail == null) {
        return "redirect:/login"; // Redirect to login page if session is not found
    }

    // Fetch all owners from the database
    List<Owners> owners = ownersRepository.findAll();

    // Add owners to the model to pass to the HTML page
    model.addAttribute("owners", owners);

    // Return the view for bookslot.html
    return "bookslot"; // Ensure this matches the HTML file name
}
@GetMapping("/slots/{ownerEmail}")
public String getOwnerSlots(@PathVariable String ownerEmail, HttpSession session, Model model) {
    // Check if the user is logged in
    String loggedInEmail = (String) session.getAttribute("loggedInEmail");
    if (loggedInEmail == null) {
        return "redirect:/login"; // Redirect to login page if session is not found
    }

    // Fetch all slots for the given owner's email
    List<SlotDetails> slots = slotDetailsRepository.findByOwnerEmail(ownerEmail);

    // Add the slots to the model to pass to the HTML page
    model.addAttribute("slots", slots);
    model.addAttribute("loggedInEmail", loggedInEmail);

    // Return the view for displaying the slots
    return "viewSlots"; // Ensure this matches the HTML file name
}
    
@PostMapping("/book")
public ResponseEntity<String> bookSlot(@RequestParam Long slotId, HttpSession session) {
    // Check if the user is logged in
    String loggedInEmail = (String) session.getAttribute("loggedInEmail");
    if (loggedInEmail == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
    }

    // Fetch the slot by ID
    Optional<SlotDetails> optionalSlot = slotDetailsRepository.findById(slotId);
    if (!optionalSlot.isPresent()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Slot not found");
    }

    SlotDetails slot = optionalSlot.get();

    // Check if the slot is already booked
    if (slot.getStatus() != null) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Slot is already booked");
    }

    // Update the slot details
    slot.setBookingDate(LocalDate.now());
    slot.setCheckinTime(LocalTime.now());
    slot.setStatus(loggedInEmail);

    // Save the updated slot
    slotDetailsRepository.save(slot);

    return ResponseEntity.ok("Slot booked successfully");
}

@PostMapping("/cancel")
public ResponseEntity<String> cancelSlot(@RequestParam Long slotId, HttpSession session) {
    // Check if the user is logged in
    String loggedInEmail = (String) session.getAttribute("loggedInEmail");
    if (loggedInEmail == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
    }

    // Fetch the slot by ID
    Optional<SlotDetails> optionalSlot = slotDetailsRepository.findById(slotId);
    if (!optionalSlot.isPresent()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Slot not found");
    }

    SlotDetails slot = optionalSlot.get();

    // Check if the slot is booked by the logged-in user
    // Fetch the owner details using the email from the slot
    Owners owner = ownersRepository.findByEmail(slot.getOwnerEmail());
    if (owner == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Owner not found");
    }

    // Calculate the cost based on the time the slot was booked
    if (slot.getBookingDate() != null && slot.getCheckinTime() != null) {
        // Calculate the duration the user has used the slot (in hours)
        long durationInHours = java.time.Duration.between(slot.getCheckinTime(), LocalTime.now()).toHours();
        if (durationInHours < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid booking time, cannot calculate duration.");
        }

        // Calculate the bill amount
        double billAmount = owner.getCostPerHour() * durationInHours;

        // Generate the bill message
        String billMessage = "Booking cancelled successfully. Bill Amount: " + billAmount;

        // Reset the slot details after displaying the bill
        slot.setBookingDate(null); // Set booking date to null
        slot.setCheckinTime(null); // Set check-in time to null
        slot.setStatus(null);      // Set status to null

        // Save the updated slot
        slotDetailsRepository.save(slot);

        // Return the bill message in the response
        return ResponseEntity.ok(billMessage);
    } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking details are incomplete, cannot generate a bill.");
    }
}

    // User logout API
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return "redirect:/login"; // Redirect to login page
    }
}
