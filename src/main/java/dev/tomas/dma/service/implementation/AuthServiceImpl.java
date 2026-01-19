package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.response.AuthRes;
import dev.tomas.dma.dto.response.AuthUserRes;
import dev.tomas.dma.dto.request.UserRegisterReq;
import dev.tomas.dma.dto.request.AuthReq;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.Status;
import dev.tomas.dma.enums.UserRole;
import dev.tomas.dma.mapper.AuthResponseMapper;
import dev.tomas.dma.repository.TicketRepo;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.AuthService;
import dev.tomas.dma.service.JWTService;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService, UserDetailsService {
    private final UserRepo userRepo;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final TicketRepo ticketRepo;

    public AuthServiceImpl(UserRepo userRepo,
                           JWTService jwtService,
                           PasswordEncoder passwordEncoder,
                           @Lazy AuthenticationManager authManager, TicketRepo ticketRepo
    ) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.ticketRepo = ticketRepo;
    }

    @Override
    public AuthRes register(UserRegisterReq request) {

        if (request.getEmail() != null && userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateKeyException("Email already exists");
        }
        if (request.getUsername() != null && userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateKeyException("Username already exists");
        }

        User toSave = new User();
        toSave.setEmail(request.getEmail());
        toSave.setPassword(passwordEncoder.encode(request.getPassword()));
        toSave.setPhoneNumber(request.getPhoneNumber());
        toSave.setAddress(request.getAddress());
        toSave.setFirstName(request.getFirstName());
        toSave.setLastName(request.getLastName());
        toSave.setMiddleNames(request.getMiddleNames());
        toSave.setUsername(request.getUsername());

        if (request.getCompanyAccount()) {
            toSave.setRole(UserRole.COMPANY_ACCOUNT);
        } else {
            toSave.setRole(UserRole.DONOR);
        }

        User user = userRepo.save(toSave);

        return new AuthRes(jwtService.generateToken(user), AuthResponseMapper.INSTANCE.convertToDTO(user));
    }

    @Override
    public AuthRes login(AuthReq authReq) {
        try {
            UsernamePasswordAuthenticationToken authRequestToken =
                    new UsernamePasswordAuthenticationToken(authReq.getUsername() == null ? authReq.getEmail() : authReq.getUsername(), authReq.getPassword());

            Authentication authentication = authManager.authenticate(authRequestToken);
            User user = (User) authentication.getPrincipal();
            AuthUserRes res = new AuthUserRes();

            res.setId(user.getId());
            res.setEmail(user.getEmail());
            res.setUsername(user.getUsername());
            res.setFirstName(user.getFirstName());
            res.setLastName(user.getLastName());
            if (user.getCompanyRole() != null) {
                res.setCompanyId(user.getCompanyRole().getCompany().getId());
                res.setCompanyRole(user.getCompanyRole().getName());
                res.setCompanyActive(!ticketRepo.existsByEntityIdAndTypeAndStatus(res.getCompanyId(), EntityType.COMPANY, Status.PENDING));
            }
            res.setRole(user.getRole().toString());

            return new AuthRes(jwtService.generateToken(user), res);

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username/email or password");
        }
    }

    public AuthUserRes authMe(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof User user)) {
            throw new BadCredentialsException("User not authenticated");
        }

        AuthUserRes res = new AuthUserRes();

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setUsername(user.getActualUsername());
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
        if (user.getCompanyRole() != null) {
            res.setCompanyId(user.getCompanyRole().getCompany().getId());
            res.setCompanyRole(user.getCompanyRole().getName());
            res.setCompanyActive(!ticketRepo.existsByEntityIdAndTypeAndStatus(res.getCompanyId(), EntityType.COMPANY, Status.PENDING));
        }
        res.setRole(user.getRole().toString());

        return res;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username).or(() -> userRepo.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));
    }
}

