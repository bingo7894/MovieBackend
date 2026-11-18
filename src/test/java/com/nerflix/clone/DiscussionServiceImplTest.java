package com.nerflix.clone;

import com.nerflix.clone.dao.EpisodeDiscussionRepository;
import com.nerflix.clone.dto.request.DiscussionReplyRequest;
import com.nerflix.clone.dto.response.DiscussionResponse;
import com.nerflix.clone.entity.*;
import com.nerflix.clone.enums.Role;
import com.nerflix.clone.serviceimpl.DiscussionServiceImpl;
import com.nerflix.clone.util.LmsMapper;
import com.nerflix.clone.util.ServiceUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscussionServiceImplTest {

    @Mock
    private EpisodeDiscussionRepository discussionRepository;

    @Mock
    private ServiceUtils serviceUtils;

    @Spy
    private LmsMapper lmsMapper = new LmsMapper();

    @InjectMocks
    private DiscussionServiceImpl discussionService;

    private EpisodeDiscussion buildDiscussion(User owner) {
        Course c = new Course();
        c.setId(1L);
        c.setPublished(true);
        Topic t = new Topic();
        t.setId(2L);
        t.setCourse(c);
        Episode ep = new Episode();
        ep.setId(3L);
        ep.setPublished(true);
        ep.setTopic(t);

        EpisodeDiscussion d = new EpisodeDiscussion();
        d.setId(50L);
        d.setEpisode(ep);
        d.setUser(owner);
        d.setTitle("How does JWT work?");
        d.setContent("Need help on filter chain.");
        return d;
    }

    @Test
    void adminReplyMarksInstructorReplyTrue() {
        User student = new User();
        student.setId(1L);
        student.setRole(Role.USER);
        student.setFullName("Student");

        User admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@lms.com");
        admin.setRole(Role.ADMIN);
        admin.setFullName("Instructor Admin");

        EpisodeDiscussion discussion = buildDiscussion(student);
        when(serviceUtils.getDiscussionByIdOrThrow(50L)).thenReturn(discussion);
        when(serviceUtils.getUserByEmailOrThrow("admin@lms.com")).thenReturn(admin);
        when(discussionRepository.save(any(EpisodeDiscussion.class))).thenAnswer(inv -> inv.getArgument(0));

        DiscussionReplyRequest req = new DiscussionReplyRequest();
        req.setContent("JwtAuthenticationFilter runs once per request.");

        DiscussionResponse resp = discussionService.addReply(50L, req, "admin@lms.com");

        assertEquals(1, resp.getReplies().size());
        assertTrue(resp.getReplies().get(0).isInstructorReply());
        assertEquals("Instructor Admin", resp.getReplies().get(0).getAuthorName());
    }

    @Test
    void nonOwnerStudentCannotDeleteAnotherStudentsDiscussion() {
        User owner = new User();
        owner.setId(1L);
        owner.setRole(Role.USER);

        User other = new User();
        other.setId(9L);
        other.setEmail("other@lms.com");
        other.setRole(Role.USER);

        EpisodeDiscussion discussion = buildDiscussion(owner);
        when(serviceUtils.getDiscussionByIdOrThrow(50L)).thenReturn(discussion);
        when(serviceUtils.getUserByEmailOrThrow("other@lms.com")).thenReturn(other);

        assertThrows(
                AccessDeniedException.class,
                () -> discussionService.deleteDiscussion(50L, "other@lms.com"));
    }
}
