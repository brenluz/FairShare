package com.brenluz.fairshare.domain.group;

import com.brenluz.fairshare.domain.user.User;
import com.brenluz.fairshare.domain.user.UserRepository;
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

    @Transactional
    public Group createGroup(String name, String description, String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = Group.builder()
                .name(name)
                .description(description)
                .createdBy(user)
                .build();

        Group saved = groupRepository.save(group);

        GroupMember member = GroupMember.builder()
                .group(saved)
                .user(user)
                .build();
        groupMemberRepository.save(member);

        return saved;
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
}
