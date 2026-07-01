package com.brenluz.fairshare.api;

import com.brenluz.fairshare.api.dto.request.CreateGroupRequest;
import com.brenluz.fairshare.api.dto.response.GroupDetailResponse;
import com.brenluz.fairshare.api.dto.response.GroupSummaryResponse;
import com.brenluz.fairshare.domain.group.Group;
import com.brenluz.fairshare.domain.group.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDetailResponse createGroup(@RequestBody CreateGroupRequest request) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                        .getName();

        Group group = groupService.createGroup(request, email);
        return GroupDetailResponse.from(group);
    }

    @PostMapping("/join/{token}")
    public GroupDetailResponse joinGroup(@PathVariable UUID token) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                        .getName();
        Group group = groupService.joinGroupByToken(token, email);
        return GroupDetailResponse.from(group);
    }

    @GetMapping("/{id}/invite")
    public String invite(@PathVariable UUID id) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getName();
        return groupService.getInviteLink(id, email);
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
