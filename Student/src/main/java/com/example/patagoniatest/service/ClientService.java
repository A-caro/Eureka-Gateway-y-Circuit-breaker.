package com.example.patagoniatest.service;

import com.example.patagoniatest.entity.Client;
import com.example.patagoniatest.feignclients.LoanFeignClient;
import com.example.patagoniatest.model.Loan;
import com.example.patagoniatest.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@Slf4j
public class ClientService implements UserDetailsService {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    LoanFeignClient loanFeignClient;

    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> getClients() {
        return clientRepository.findAll();
    }

    public Client addClient(Client client) {
        return clientRepository.save(client);
    }

    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }

    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    public void updateCliente(Long id, Client cliente){
        Optional<Client> client = clientRepository.findById(id);
        try{
            client.isPresent();
            if (!client.get().equals(cliente.getFullName())){
                client.get().setFullName(cliente.getFullName());
            } if(client.get().equals(cliente.getIncome())){
                client.get().setIncome(cliente.getIncome());
            }
    }catch (IllegalStateException e){
            System.out.println("El id solicitado no existe" + e.getMessage());
        }
        clientRepository.save(client.get());
    }

    public Loan saveLoan(Long clientId, Loan loan){
        loan.setClientId(clientId);
        Loan loanNew = loanFeignClient.saveLoan(loan);
        return loanNew;
    }

   public String getDatos(){
        List<Client> clients  = clientRepository.findAll();
        return clients.stream().map(s -> s.getId() + " , " + s.getFullName() + " , " + s.getIncome()).collect(Collectors.joining("\n"));
    }


    @Override
    public UserDetails loadUserByUsername(String fullName) throws UsernameNotFoundException {
        Client client = clientRepository.findByFullName(fullName);
        if(client == null){
            log.error("Employee not found");
            throw new UsernameNotFoundException("ERROR! Client not found");
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        client.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
        return new org.springframework.security.core.userdetails
                .User(client.getFullName(), client.getPassword(), authorities);
    }


/*    public Role saveRole(Role role){
        log.info("saving role to the database");
        return roleRepository.save(role);
    }

    @Transactional
    public void addRoleToClient(String fullName, String roleName){
        log.info("Adding {} role to user: {}", roleName, fullName);
        Client client = clientRepository.findByFullName(fullName);
        Role role = roleRepository.findByName(roleName);
        client.getRoles().add(role);
    }*/




}
