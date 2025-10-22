package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.response.AuthRes;
import dev.tomas.dma.dto.response.AuthUserRes;
import dev.tomas.dma.dto.request.UserRegisterReq;
import dev.tomas.dma.dto.request.AuthReq;
import dev.tomas.dma.dto.response.MembershipGetRes;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.mapper.AuthResponseMapper;
import dev.tomas.dma.repository.AuthRepo;
import dev.tomas.dma.service.AuthService;
import dev.tomas.dma.service.CompanyService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService, UserDetailsService {
    private final AuthRepo authRepo;
    private final JWTService jwtService;
    private final CompanyService companyService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    public AuthServiceImpl(AuthRepo authRepo,
                           JWTService jwtService,
                           PasswordEncoder passwordEncoder,
                           CompanyService companyService,
                           @Lazy AuthenticationManager authManager
    ) {
        this.authRepo = authRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.companyService = companyService;
        this.authManager = authManager;
    }

    @Override
    public AuthRes register(UserRegisterReq registerRequest) {

        if (registerRequest.getEmail() != null && authRepo.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (registerRequest.getUsername() != null && authRepo.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User toSave = new User();
        toSave.setEmail(registerRequest.getEmail());
        toSave.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        toSave.setPhoneNumber(registerRequest.getPhoneNumber());
        toSave.setAddress(registerRequest.getAddress());
        toSave.setFirstName(registerRequest.getFirstName());
        toSave.setLastName(registerRequest.getLastName());
        toSave.setMiddleNames(registerRequest.getMiddleNames());
        toSave.setUsername(registerRequest.getUsername());

        User createdUser = authRepo.save(toSave);
        return new AuthRes(jwtService.generateToken(createdUser), AuthResponseMapper.INSTANCE.convertToModel(createdUser));
    }

    @Override
    public AuthRes login(AuthReq authReq) {
        try {
            UsernamePasswordAuthenticationToken authRequestToken =
                    new UsernamePasswordAuthenticationToken(authReq.getUsername() == null ? authReq.getEmail() : authReq.getUsername(), authReq.getPassword());

            Authentication authentication = authManager.authenticate(authRequestToken);
            User user = (User) authentication.getPrincipal();

            return new AuthRes(jwtService.generateToken(user), AuthResponseMapper.INSTANCE.convertToModel(user));

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username/email or password");
        }
    }

    public AuthUserRes authMe(Authentication authentication) {
        String username = authentication.getName();
        User user = authRepo.findByUsername(username)
                .or(() -> authRepo.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        Optional<MembershipGetRes> membership = companyService.getMembershipByUserId(user.getId());

        AuthUserRes res = new AuthUserRes();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setUsername(user.getUsername());
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
        res.setCompanyId(membership.get().getCompanyId());
        res.setRole(membership.get().getRole());

        return res;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authRepo.findByUsername(username)
                .or(() -> authRepo.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));
    }
}

