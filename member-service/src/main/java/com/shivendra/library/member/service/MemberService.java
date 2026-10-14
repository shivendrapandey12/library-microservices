package com.shivendra.library.member.service;

import com.shivendra.library.member.dto.AuthResponse;
import com.shivendra.library.member.dto.LoginRequest;
import com.shivendra.library.member.dto.MemberResponse;
import com.shivendra.library.member.dto.RegisterRequest;
import com.shivendra.library.member.exception.EmailAlreadyUsedException;
import com.shivendra.library.member.exception.InvalidCredentialsException;
import com.shivendra.library.member.exception.MemberNotFoundException;
import com.shivendra.library.member.model.Member;
import com.shivendra.library.member.model.Role;
import com.shivendra.library.member.repository.MemberRepository;
import com.shivendra.library.member.security.JwtService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository members;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public MemberService(MemberRepository members, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.members = members;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Public registration always creates a USER; ADMIN accounts are created by seeding only. */
    @Transactional
    public MemberResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (members.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        Member member = new Member(
                request.name(), email, passwordEncoder.encode(request.password()), Role.USER);
        return MemberResponse.from(members.save(member));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Member member = members.findByEmail(request.email().trim().toLowerCase())
                .filter(Member::isActive)
                .filter(m -> passwordEncoder.matches(request.password(), m.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return new AuthResponse(jwtService.generate(member), member.getRole().name(), member.getId());
    }

    @Transactional(readOnly = true)
    public MemberResponse get(Long id) {
        return members.findById(id)
                .map(MemberResponse::from)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return members.findAll().stream().map(MemberResponse::from).toList();
    }
}
