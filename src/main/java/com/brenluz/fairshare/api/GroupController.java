package com.brenluz.fairshare.api;

import com.brenluz.fairshare.api.dto.request.CreateGroupRequest;
import com.brenluz.fairshare.api.dto.response.GroupDetailResponse;
import com.brenluz.fairshare.api.dto.response.GroupSummaryResponse;
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

    @PostMapping()
    public GroupDetailResponse createGroup(@RequestBody CreateGroupRequest request) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                        .getName();

        Group group = groupService.createGroup(request.name(), request.description(), email);
        return GroupDetailResponse.from(group);
    }

    @GetMapping()
    public List<GroupSummaryResponse> getUserGroups(){
        String email = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getName();

        return groupService.getUserGroups(email)
                .stream()
                .map(GroupSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public GroupDetailResponse getGroup(@PathVariable UUID id){
        Group group = groupService.findById(id).orElseThrow(() -> new RuntimeException("Group not found"));
        return GroupDetailResponse.from(group);
    }
}
