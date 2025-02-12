package com.angorasix.clubs

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["configs.tokens.secret=secretwiththirtytwocharacterslong"])
class ClubsApplicationTests {

    @Test
    fun contextLoads() {
        // Empty, just to check that context loads
    }
}
