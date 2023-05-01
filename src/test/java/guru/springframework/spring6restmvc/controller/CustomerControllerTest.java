package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.services.CustomerService;
import guru.springframework.spring6restmvc.services.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.core.Is.is;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @MockBean
    CustomerService customerService;

    // we create mockmvc that will be my actual testing component
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;


    CustomerServiceImpl customerServiceImpl;

    @BeforeEach
    void setUp() {
        customerServiceImpl = new CustomerServiceImpl();
    }

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<CustomerDTO> customerArgumentCaptor;

    @Test
    void testPatchCustomer() throws Exception {

        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("name", "NewCustomerName");

        mockMvc.perform(patch( CustomerController.CUSTOMER_PATH+"/" + customerDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hashMap)))
                .andExpect(status().isNoContent());

        verify(customerService).patchCustomerById(uuidArgumentCaptor.capture(), customerArgumentCaptor.capture());

        assertThat(customerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(hashMap.get("name")).isEqualTo(customerArgumentCaptor.getValue().getName());
    }

    @Test
    void testDeleteCustomer() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);

        given(customerService.deleteCustomerById(any())).willReturn(true);

        mockMvc.perform(delete(CustomerController.CUSTOMER_PATH +"/" + customerDTO.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomerById(uuidArgumentCaptor.capture());

        assertThat(customerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());

        //Dog fido = new Dog("Fido", 5.25);
        //Dog fidosClone = new Dog("Fido", 5.25);
        //assertThat(fido).isEqualTo(fidosClone);
        //This will fail as isEqualTo() compares object references.
        //If we want to compare their content instead, we can use isEqualToComparingFieldByFieldRecursively()
        //assertThat(fido).isEqualToComparingFieldByFieldRecursively(fidosClone);
        //Fido and fidosClone are equal when doing a recursive field by field comparison
        //because each field of one object is compared to the field in the other object

    }

    @Test
    void testUpdateCustomer() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);

        given(customerService.updateCustomerById(any(), any())).willReturn(Optional.of(CustomerDTO.builder().build()));

        mockMvc.perform(put(CustomerController.CUSTOMER_PATH + "/" + customerDTO.getId())
                .content(objectMapper.writeValueAsString(customerDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(customerService).updateCustomerById(uuidArgumentCaptor.capture(), any(CustomerDTO.class));
        assertThat(customerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void testCreateCustomer() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);
        customerDTO.setId(null);
        customerDTO.setVersion(null);

        given(customerService.saveNewCustomer(any(CustomerDTO.class)))
                .willReturn(customerServiceImpl.getAllCustomers().get(1));

        mockMvc.perform(post(CustomerController.CUSTOMER_PATH).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void listAllCustomers() throws Exception {
        // settup mockito
        given(customerService.getAllCustomers()).willReturn(customerServiceImpl.getAllCustomers());

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    void getCustomerByIdNotFound() throws Exception {

        given(customerService.getCustomerById(any(UUID.class))).willThrow(NotFoundException.class);

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomerById() throws Exception {

        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);

        given(customerService.getCustomerById(customerDTO.getId())).willReturn(customerDTO);

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH + "/" + customerDTO.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(customerDTO.getName())));
    }
}