package com.nerflix.clone.serviceimpl;

import com.nerflix.clone.dao.CourseCertificateRepository;
import com.nerflix.clone.dao.CourseReviewRepository;
import com.nerflix.clone.dao.DiscussionReplyRepository;
import com.nerflix.clone.dao.EnrollmentRepository;
import com.nerflix.clone.dao.EpisodeDiscussionRepository;
import com.nerflix.clone.dao.LessonNoteRepository;
import com.nerflix.clone.dao.LessonProgressRepository;
import com.nerflix.clone.dao.QuizAttemptRepository;
import com.nerflix.clone.dao.UserRepository;
import com.nerflix.clone.dto.request.UserRequest;
import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.PageResponse;
import com.nerflix.clone.dto.response.UserResponse;
import com.nerflix.clone.entity.User;
import com.nerflix.clone.enums.Role;
import com.nerflix.clone.exception.EmailAlreadyExistsException;
import com.nerflix.clone.exception.InvalidRoleException;
import com.nerflix.clone.service.EmailService;
import org.springframework.data.domain.Pageable;
import com.nerflix.clone.service.UserService;
import com.nerflix.clone.util.PaginationUtils;
import com.nerflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private LessonNoteRepository lessonNoteRepository;

    @Autowired
    private CourseCertificateRepository courseCertificateRepository;

    @Autowired
    private CourseReviewRepository courseReviewRepository;

    @Autowired
    private EpisodeDiscussionRepository episodeDiscussionRepository;

    @Autowired
    private DiscussionReplyRepository discussionReplyRepository;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Override
    @Transactional
    public MessageResponse createUser(UserRequest userRequest) {
        if(userRepository.findByEmail(userRequest.getEmail()).isPresent()){
            throw new EmailAlreadyExistsException("Email already exists");
        }

        validateRole(userRequest.getRole());

        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setFullName(userRequest.getFullname());
        user.setRole(Role.valueOf(userRequest.getRole().toUpperCase()));
        user.setActive(true);
        if (!mailEnabled) {
            user.setEmailVerified(true);
            userRepository.save(user);
            return new MessageResponse("User created successfully.");
        }
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(86400));
        userRepository.save(user);
        emailService.sendVerificationEmail(userRequest.getEmail(),verificationToken);
        return new MessageResponse("User create successfully. ");
    }

    private void validateRole(String role) {
        if(Arrays.stream(Role.values()).noneMatch(r->r.name().equalsIgnoreCase(role))){
            throw new InvalidRoleException("Invalid role" +role);
        }
    }

    @Override
    public MessageResponse updateUser(Long id, UserRequest userRequest) {
        User user = serviceUtils.getUserByIdOrThrow(id);

        ensureNotLastActiveAdmin(user);
        validateRole(userRequest.getRole());

        user.setFullName(userRequest.getFullname());
        user.setRole(Role.valueOf(userRequest.getRole().toUpperCase()));
        userRepository.save(user);
        return new MessageResponse("User update successfully. ");
        }

    private void ensureNotLastActiveAdmin(User user) {

        if(user.isActive() && user.getRole() == Role.ADMIN){
            long activeAdminCount = userRepository.countByRoleAndActive(Role.ADMIN,true);
            if(activeAdminCount <= 1){
                throw new RuntimeException("Cannot deactivate the last active admin user");
            }
         }
    }

    @Override
    public PageResponse<UserResponse> getUsers(int page, int size, String search) {

        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");

        Page<User> userPage;

        if(search != null && !search.trim().isEmpty()){
            userPage = userRepository.searchUsers(search.trim(),pageable);
        }else{
            userPage = userRepository.findAll(pageable);
        }

        return PaginationUtils.toPageResponse(userPage, UserResponse::fromEntity);

    }

    @Override
    @Transactional
    public MessageResponse deleteUser(Long id, String currentUserEmail) {
        User user = serviceUtils.getUserByIdOrThrow(id);

        if(user.getEmail().equals(currentUserEmail)){
            throw new RuntimeException("You cannot delete your own account");
        }

        ensureNotLastActiveAdmin(user,"delete");

        discussionReplyRepository.deleteByUserId(id);
        var userDiscussionIds = episodeDiscussionRepository.findIdsByUserId(id);
        if (!userDiscussionIds.isEmpty()) {
            discussionReplyRepository.deleteByDiscussionIdIn(userDiscussionIds);
        }
        episodeDiscussionRepository.deleteByUserId(id);
        courseReviewRepository.deleteByUserId(id);
        courseCertificateRepository.deleteByUserId(id);
        lessonNoteRepository.deleteByUserId(id);
        lessonProgressRepository.deleteByUserId(id);
        quizAttemptRepository.deleteByUserId(id);
        enrollmentRepository.deleteByUserId(id);

        userRepository.deleteById(id);
        return new MessageResponse("User deleted successfully. ");
    }


    private  void ensureNotLastActiveAdmin(User user,String operation){
        if(user.getRole()==Role.ADMIN){
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if(adminCount <=1 ){
                throw  new RuntimeException("Cannot "  + operation + " the last admin user");
            }
        }
    }

    @Override
    public MessageResponse toggleUserStatus(Long id, String currentUserEmail) {
        User user = serviceUtils.getUserByIdOrThrow(id);

        if(user.getEmail().equals(currentUserEmail)){
            throw new RuntimeException("You cannot deactivate your own account");
        }

        ensureNotLastActiveAdmin(user);

        user.setActive(!user.isActive());
        userRepository.save(user);
        return new MessageResponse("User status update successfully");

    }

    @Override
    public MessageResponse changeRole(Long id, UserRequest userRequest) {
        User user = serviceUtils.getUserByIdOrThrow(id);
        validateRole(userRequest.getRole());

        Role newRole = Role.valueOf(userRequest.getRole().toUpperCase());
        if(user.getRole()==Role.ADMIN && newRole == Role.USER){
            ensureNotLastActiveAdmin(user,"change the role of");
        }

        user.setRole(newRole);
        userRepository.save(user);
        return new MessageResponse("User role update successfully. ");
    }

}
