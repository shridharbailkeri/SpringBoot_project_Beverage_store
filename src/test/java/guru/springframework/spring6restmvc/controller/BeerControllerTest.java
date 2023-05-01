package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//@SpringBootTest
@WebMvcTest(BeerController.class)
class BeerControllerTest {

    //@Autowired
    //BeerController beerController;
    // mockmvc context
    // we create mockmvc that will be my actual testing component
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerService beerService;

    // make mockito to return back data , now we can write data again but we do have our implementation
    // //of beerservive impl
    //set up mockito to return back data
    BeerServiceImpl beerServiceImpl;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<BeerDTO> beerArgumentCaptor;

    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
        // so this is going to reset that
        // so now the setup method is going to run before each so we can modify that
        // beer list implementation in the test testCreateNewBeer() , so when the next test runs it will be OK
        // because we will have  a new object assigned to that
    }

    @Test
    void testPatchBeer() throws Exception {

        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

        Map<String, Object> beerMap = new HashMap<>();

        // key becomes the json property
        beerMap.put("beerName", "New Name");
        // now we get a simple json object being passed in with just beer name, which is really
        // mimicking what a client would do when we're using a patch operation
        mockMvc.perform(patch(BeerController.BEER_PATH_ID, beerDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());

        assertThat(beerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(beerMap.get("beerName")).isEqualTo(beerArgumentCaptor.getValue().getBeerName());

    }

    @Test
    void testDeleteBeer() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

        given(beerService.deleteById(any())).willReturn(true);

        mockMvc.perform(delete(BeerController.BEER_PATH_ID, beerDTO.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());


        verify(beerService).deleteById(uuidArgumentCaptor.capture());

        assertThat(beerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void testUpdateBeer() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);
        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beerDTO));
        mockMvc.perform(put(BeerController.BEER_PATH_ID, beerDTO.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isNoContent());

        verify(beerService).updateBeerById(any(UUID.class), any(BeerDTO.class));

    }

    @Test
    void testUpdateBeerBlankName() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);
        beerDTO.setBeerName("");
        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beerDTO));
        mockMvc.perform(put(BeerController.BEER_PATH_ID, beerDTO.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)));

    }

    @Test
    void testCreateNewBeer() throws Exception {
        // less prefered way commented out no need for this to have autowired objectmapper as above
        //ObjectMapper objectMapper = new ObjectMapper();
        //objectMapper.findAndRegisterModules();

        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);
        // set up mock mvc to pass in that beer object as json into the controller
        // and allow the controller to create action
        beerDTO.setVersion(null);
        beerDTO.setId(null);

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(1));

        mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testCreateBeerNullBeerName() throws Exception {


        BeerDTO beerDTO = BeerDTO.builder().build();

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(1));

        MvcResult mvcResult = mockMvc.perform(post(BeerController.BEER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6)))
                .andReturn();

    // mvcResult holds inside in it MethodArgumentNotValidException
        System.out.println(mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testListBeers() throws Exception {
        // work with mockito to give back list of beers
        given(beerService.listBeers(any(), any(), any(), any(), any()))
                .willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25));

        // now we r going to do mockmvc, perform and do a get
        mockMvc.perform(get("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", is(3)));
    }

    @Test
    void getBeerByIdNotFound() throws Exception {

        given(beerService.getBeerById(any(UUID.class))).willThrow(NotFoundException.class);

        mockMvc.perform(get(BeerController.BEER_PATH_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBeerById() throws Exception {

        BeerDTO testBeerDTO = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);
        // am asking for the first beer from the list (testBeer) above and below (given) thats where Im
        // configuring Maketo to go ahead and return back that testBeer Object
        // so i get back testBeer obj from service implementation and then tell mockito to return it
        //any(Foo.class) returns an argument of type Foo, not of type Class<Foo>
        //Sometimes we want to mock the behavior for any argument of the given type, in that case, we can
        //use Mockito argument matchers
        given(beerService.getBeerById(testBeerDTO.getId())).willReturn(testBeerDTO);
        // here we r telling mockito given beerservice get by id and any UUID being passed in its
        // gonna return back that testBeer object

        // below u perform get on get("/api/v1/beer/"+ UUID.randomUUID()
        // u r telling accept JSON Body as input if get JASON body say status OK
        // as by default mockito returns null , to retrun a value u configured above
        // andExpect means out put returned a JASON check if u got a json back as return value

        mockMvc.perform(get("/api/v1/beer/"+ testBeerDTO.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBeerDTO.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeerDTO.getBeerName())));

        // "$.id" thats meaning from the root
        //System.out.println(beerController.getBeerById(UUID.randomUUID()));
    }
}