package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.response.AuthRes;
import dev.tomas.dma.dto.response.AuthUserRes;
import dev.tomas.dma.dto.request.UserRegisterReq;
import dev.tomas.dma.dto.request.AuthReq;
import dev.tomas.dma.dto.response.MembershipGetRes;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.mapper.AuthResponseMapper;
import dev.tomas.dma.model.UserCompanyMembershipModel;
import dev.tomas.dma.model.UserModel;
import dev.tomas.dma.repository.AuthRepo;
import dev.tomas.dma.service.AuthService;
import dev.tomas.dma.service.CompanyService;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
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
            throw new DuplicateKeyException("Email already exists");
        }
        if (registerRequest.getUsername() != null && authRepo.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new DuplicateKeyException("Username already exists");
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
        UserModel user = new UserModel(createdUser);

        return new AuthRes(jwtService.generateToken(user), AuthResponseMapper.INSTANCE.convertToDTO(user));
    }

    @Override
    public AuthRes login(AuthReq authReq) {
        try {
            UsernamePasswordAuthenticationToken authRequestToken =
                    new UsernamePasswordAuthenticationToken(authReq.getUsername() == null ? authReq.getEmail() : authReq.getUsername(), authReq.getPassword());

            Authentication authentication = authManager.authenticate(authRequestToken);
            UserModel user = (UserModel) authentication.getPrincipal();

            Optional<UserCompanyMembershipModel> membership = companyService.getMembershipByUserId(user.getId());

            if (membership.isPresent()) {
                user.setCompanyId(membership.get().getCompanyId());
                user.setCompanyRole(membership.get().getCompanyRole());
            }

            AuthUserRes res = new AuthUserRes();
            res.setId(user.getId());
            res.setEmail(user.getEmail());
            res.setUsername(user.getUsername());
            res.setFirstName(user.getFirstName());
            res.setLastName(user.getLastName());
            res.setCompanyId(user.getCompanyId());
            res.setCompanyRole(user.getCompanyRole());

            return new AuthRes(jwtService.generateToken(user), res);

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username/email or password");
        }
    }

    public AuthUserRes authMe(Authentication authentication) {
        UserModel user = (UserModel) authentication.getPrincipal();
        Optional<UserCompanyMembershipModel> membership = companyService.getMembershipByUserId(user.getId());

        if (membership.isPresent()) {
            user.setCompanyId(membership.get().getCompanyId());
            user.setCompanyRole(membership.get().getCompanyRole());
        }

        AuthUserRes res = new AuthUserRes();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setUsername(user.getUsername());
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
        res.setCompanyId(user.getCompanyId());
        res.setCompanyRole(user.getCompanyRole());

        return res;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userEntity = authRepo.findByUsername(username).or(() -> authRepo.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));
        UserModel user = new UserModel(userEntity);
        return user;
    }
}

