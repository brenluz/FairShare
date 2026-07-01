package com.brenluz.fairshare.domain.group;

import com.brenluz.fairshare.api.dto.request.CreateGroupRequest;
import com.brenluz.fairshare.domain.user.User;
import com.brenluz.fairshare.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final EntityManager entityManager;

    @Transactional
    public Group createGroup(CreateGroupRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = Group.builder()
                .name(request.name())
                .description(request.description())
                .createdBy(user)
                .build();

        Group saved = groupRepository.save(group);

        GroupMember member = GroupMember.builder()
                .group(saved)
                .user(user)
                .build();
        groupMemberRepository.save(member);

        entityManager.flush();
        entityManager.clear();

        return groupRepository.findById(saved.getId())
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    @Transactional(readOnly = true)
    public List<Group> getUserGroups(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return groupMemberRepository.findByUser(user)
                .stream()
                .map(GroupMember::getGroup)
                .toList();

    }

    @Transactional(readOnly = true)
    public Optional<Group> findById(UUID id) {
        return groupRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Group findByInviteToken(UUID token) {
        return groupRepository.findByInviteToken(token).orElseThrow(() -> new RuntimeException("Group not found"));
    }

    @Transactional(readOnly = true)
    public String getInviteLink(UUID groupId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        boolean isMember = group.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        if (!isMember) {
            throw new RuntimeException("You are not a member of this group");
        }

        return "http://localhost:8080/api/groups/join/" + group.getInviteToken();
    }

    @Transactional
    public Group joinGroupByToken(UUID token, String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findByInviteToken(token).orElseThrow(() -> new RuntimeException("Group not found"));

        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        if (alreadyMember) {
            return group;
        }

        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .user(user)
                .build();

        groupMemberRepository.save(groupMember);

        entityManager.flush();
        entityManager.clear();
        
        return groupRepository.findById(group.getId())
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }
}
