package com.github.irybov.bankdemoboot.dao;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
//import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
//import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
//import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.security.Role;

//@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
class AccountDAOTest {
	
	@PersistenceContext
	private EntityManager entityManager;
	@InjectMocks
	private AccountDAO accountDAO;

	private String oldPN;
	private String newPN;
	private Account account;
	
	@BeforeAll
	void prepare() {
		ReflectionTestUtils.setField(accountDAO, "entityManager", entityManager);
		oldPN = new String("0000000000");
		newPN = new String("9999999999");
	}
	
	@BeforeEach
	void set_up() {
		entityManager.createNativeQuery("DELETE FROM {h-schema}bills").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM {h-schema}roles").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM {h-schema}accounts").executeUpdate();
		account = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true);
		account.addRole(Role.ADMIN);
		accountDAO.saveAccount(account);
	}
	
	@Test
//	@Order(1)
	void check_that_phone_present() {
        assertThat(accountDAO.getPhone(oldPN)).isEqualTo(account.getPhone());
	}
	
    @Test
//	@Order(2)
	void check_that_phone_not_present() {
//		assertThatExceptionOfType(NoResultException.class)
//		.isThrownBy(() -> {accountDAO.getPhone(newPN);});
		assertThat(accountDAO.getPhone(newPN)).isNull();
	}
	
	@Test
//	@Order(3)
	void search_by_phone() {
		Account fromDB = accountDAO.getAccount(oldPN);
		assertThat(fromDB).isEqualTo(account);
	}

	@Test
//	@Order(4)
	void update_and_compare() {
		Account fromDB = accountDAO.getAccount(oldPN);
		fromDB.setPhone(newPN);
		accountDAO.updateAccount(fromDB);
		Account updated = accountDAO.getAccount(newPN);		
		assertThat(fromDB).isEqualTo(updated);		
	}
    
    @Test
//	@Order(5)
    void check_for_clients_presence() {
    	List<Account> clients = accountDAO.getAll();
    	assertThat(clients).isNotNull();
    	assertThat(clients.isEmpty()).isTrue();
    	
		Account vixenEntity = new Account
		("Marica", "Hase", "1111111111", LocalDate.of(1981, Month.SEPTEMBER, 26), "supervixen", true);
		vixenEntity.addRole(Role.CLIENT);
		Account blondeEntity = new Account
		("Sarah", "Vandella", "2222222222", LocalDate.of(1983, Month.DECEMBER, 02), "bustyblonde", true);
		blondeEntity.addRole(Role.CLIENT);
		Account gingerEntity = new Account
		("Lily", "Cade", "3333333333", LocalDate.of(1995, Month.JANUARY, 25), "gingerchick", true);
		gingerEntity.addRole(Role.CLIENT);
		gingerEntity.addRole(Role.ADMIN);
		
    	List<Account> whores = new ArrayList<>();
    	whores.add(vixenEntity);
    	whores.add(blondeEntity);
    	whores.add(gingerEntity);
    	whores.forEach(accountDAO::saveAccount);
    	
    	clients = accountDAO.getAll();
    	assertThat(clients.isEmpty()).isFalse();
    	assertThat(clients.size()).isEqualTo(whores.size());
    }
    
    @Test
//	@Order(6)
    void produce_persistence_exception() {
		Account fakeAdmin = new Account
				("Admin", "Adminov", "0000000000", LocalDate.of(2001, 01, 01), "superadmin", true);
		fakeAdmin.addRole(Role.CLIENT);
		assertThatExceptionOfType(PersistenceException.class)
		.isThrownBy(() -> {accountDAO.saveAccount(fakeAdmin);});
    }
	
    @AfterEach
    void tear_down() {
    	accountDAO.deleteAccount(account);
    	account = null;
    }
    
    @AfterAll
    void clear() {
    	oldPN = null;
    	newPN = null;
    }
    
}
