package com.brenluz.fairshare.api;

import com.brenluz.fairshare.api.dto.request.CreateGroupRequest;
import com.brenluz.fairshare.api.dto.response.GroupResponse;
import com.brenluz.fairshare.domain.group.Group;
import com.brenluz.fairshare.domain.group.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor()
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/")
    public GroupResponse createGroup(@RequestBody CreateGroupRequest request) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                        .getName();

        Group group = groupService.createGroup(request.name(), request.description(), email);
        return GroupResponse.from(group);
    }

    @GetMapping("/")
    public List<GroupResponse> getUserGroups(){
        String email = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getName();

        return groupService.getUserGroups(email)
                .stream()
                .map(GroupResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public GroupResponse getGroup(@PathVariable UUID id){
        Group group = groupService.findById(id).orElseThrow(() -> new RuntimeException("Group not found"));
        return GroupResponse.from(group);
    }
}
