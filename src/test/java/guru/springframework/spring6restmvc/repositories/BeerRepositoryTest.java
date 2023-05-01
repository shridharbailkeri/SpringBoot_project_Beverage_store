package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.bootstrap.BootstrapData;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.services.BeerCsvServiceImpl;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@Import({BootstrapData.class, BeerCsvServiceImpl.class})
// to wire in the beercsv service for example to list beer by name video 151
class BeerRepositoryTest {
    @Autowired
    BeerRepository beerRepository;

    @Test
    void testGetBeerListByName() {
        //%% is sql convention
        // we r going to do search within the databse , so we need to use the wildcard  %
        // i want to rerurn any name that contains the phrase IPA
        // if i add percent signs those are wild cards so what i am saying with the syntax is to return me
        // back a list of beers anywhere in that string that beername contains the string ipa
        Page<Beer> list = beerRepository.findAllByBeerNameIsLikeIgnoreCase("%IPA%", null);

        assertThat(list.getContent().size()).isEqualTo(336);
    }

    @Test
    void testSaveBeerNameTooLong() {

        assertThrows(ConstraintViolationException.class, () -> {
            Beer savedBeer = beerRepository.save(Beer.builder()
                    .beerName("My Beer 012456789012456789012456789012456789012456789012456789012456789")
                    .beerStyle(BeerStyle.PALE_ALE)
                    .upc("23456567")
                    .price(new BigDecimal("11.99"))
                    .build());

            // this tells hibernate to immediately write to the database
            // if beername style, upc price are not set the test will fail because of below statement
            beerRepository.flush();
        });

    }

    @Test
    void testSaveBeer() {

        Beer savedBeer = beerRepository.save(Beer.builder()
                .beerName("My Beer")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("23456567")
                .price(new BigDecimal("11.99"))
                .build());

        // this tells hibernate to immediately write to the database
        // if beername style, upc price are not set the test will fail because of below statement
        beerRepository.flush();

        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
    }
}