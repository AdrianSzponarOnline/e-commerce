package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.address.AddressCreateDTO;
import com.ecommerce.E_commerce.dto.address.AddressDTO;
import com.ecommerce.E_commerce.dto.address.AddressUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.AddressMapper;
import com.ecommerce.E_commerce.model.Address;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.repository.AddressRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");

        testAddress = new Address();
        testAddress.setId(1L);
        testAddress.setUser(testUser);
        testAddress.setLine1("123 Main St");
        testAddress.setLine2("Apt 4");
        testAddress.setCity("Warsaw");
        testAddress.setRegion("Mazovia");
        testAddress.setPostalCode("00-001");
        testAddress.setCountry("Poland");
        testAddress.setCreatedAt(Instant.now());
        testAddress.setUpdatedAt(Instant.now());
        testAddress.setIsActive(true);

        // Mock SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn("user@example.com");
        lenient().doReturn(Set.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void create_ShouldReturnAddressDTO_WhenSuccessful() {
        // Given
        AddressCreateDTO dto = new AddressCreateDTO(
                1L, "123 Main St", "Apt 4", "Warsaw", "Mazovia", "00-001", "Poland", true
        );
        Address address = new Address();
        AddressDTO expectedDTO = new AddressDTO(1L, 1L, "123 Main St", "Apt 4", "Warsaw", "Mazovia", "00-001", "Poland",
                Instant.now(), Instant.now(), null, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressMapper.toAddress(dto)).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(addressMapper.toAddressDTO(testAddress)).thenReturn(expectedDTO);

        // When
        AddressDTO result = addressService.create(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void create_ShouldSetActiveToTrue_WhenIsActiveNotProvided() {
        // Given
        AddressCreateDTO dto = new AddressCreateDTO(
                1L, "123 Main St", null, "Warsaw", null, "00-001", "Poland", null
        );
        Address address = new Address();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressMapper.toAddress(dto)).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(addressMapper.toAddressDTO(any())).thenReturn(
                new AddressDTO(1L, 1L, "123 Main St", null, "Warsaw", null, "00-001", "Poland",
                        Instant.now(), Instant.now(), null, true)
        );

        // When
        addressService.create(dto);

        // Then
        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isTrue();
    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        // Given
        AddressCreateDTO dto = new AddressCreateDTO(
                1L, "123 Main St", null, "Warsaw", null, "00-001", "Poland", true
        );

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> addressService.create(dto));
        verify(addressRepository, never()).save(any());
    }

    @Test
    void update_ShouldReturnUpdatedAddressDTO_WhenSuccessful() {
        // Given
        AddressUpdateDTO dto = new AddressUpdateDTO(
                "456 New St", null, "Krakow", null, "30-001", "Poland", false
        );
        AddressDTO expectedDTO = new AddressDTO(1L, 1L, "456 New St", null, "Krakow", null, "30-001", "Poland",
                Instant.now(), Instant.now(), null, false);

        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(addressMapper.toAddressDTO(any(Address.class))).thenReturn(expectedDTO);

        // When
        AddressDTO result = addressService.update(1L, dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.city()).isEqualTo("Krakow");
        verify(addressRepository).findById(1L);
        verify(addressMapper).updateAddressFromDTO(dto, testAddress);
        verify(addressRepository).save(testAddress);
    }

    @Test
    void update_ShouldThrowException_WhenAddressNotFound() {
        // Given
        AddressUpdateDTO dto = new AddressUpdateDTO(null, null, "Krakow", null, null, null, null);

        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> addressService.update(1L, dto));
    }

    @Test
    void delete_ShouldSetDeletedAtAndIsActive_WhenSuccessful() {
        // Given
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));

        // When
        addressService.delete(1L);

        // Then
        verify(addressRepository).findById(1L);
        verify(addressRepository).delete(testAddress);
    }

    @Test
    void delete_ShouldThrowException_WhenAddressNotFound() {
        // Given
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> addressService.delete(1L));
    }

    @Test
    void getById_ShouldReturnAddressDTO_WhenFound() {
        // Given
        AddressDTO expectedDTO = new AddressDTO(1L, 1L, "123 Main St", null, "Warsaw", null, "00-001", "Poland",
                Instant.now(), Instant.now(), null, true);

        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(addressMapper.toAddressDTO(testAddress)).thenReturn(expectedDTO);

        // When
        AddressDTO result = addressService.getById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(addressRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        // Given
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> addressService.getById(1L));
    }

    @Test
    void getByUserId_ShouldReturnListOfAddresses() {
        // Given
        Address address2 = new Address();
        address2.setId(2L);
        address2.setUser(testUser);
        address2.setLine1("789 Other St");

        when(addressRepository.findByUserId(1L)).thenReturn(List.of(testAddress, address2));
        when(addressMapper.toAddressDTO(testAddress)).thenReturn(
                new AddressDTO(1L, 1L, "123 Main St", null, "Warsaw", null, "00-001", "Poland",
                        Instant.now(), Instant.now(), null, true)
        );
        when(addressMapper.toAddressDTO(address2)).thenReturn(
                new AddressDTO(2L, 1L, "789 Other St", null, "Warsaw", null, "00-001", "Poland",
                        Instant.now(), Instant.now(), null, true)
        );

        // When
        List<AddressDTO> result = addressService.getByUserId(1L);

        // Then
        assertThat(result).hasSize(2);
        verify(addressRepository).findByUserId(1L);
    }

    @Test
    void getActiveByUserId_ShouldReturnOnlyActiveAddresses() {
        // Given
        Address inactiveAddress = new Address();
        inactiveAddress.setId(2L);
        inactiveAddress.setUser(testUser);
        inactiveAddress.setIsActive(false);

        when(addressRepository.findByUserIdAndIsActive(1L, true)).thenReturn(List.of(testAddress));
        when(addressMapper.toAddressDTO(testAddress)).thenReturn(
                new AddressDTO(1L, 1L, "123 Main St", null, "Warsaw", null, "00-001", "Poland",
                        Instant.now(), Instant.now(), null, true)
        );

        // When
        List<AddressDTO> result = addressService.getActiveByUserId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
        verify(addressRepository).findByUserIdAndIsActive(1L, true);
    }

    // Note: isOwner method was removed from AddressServiceImpl
    // Authorization is now handled in AddressController using @AuthenticationPrincipal
}

