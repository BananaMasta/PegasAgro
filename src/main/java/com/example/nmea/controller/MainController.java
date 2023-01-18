package com.example.nmea.controller;

import com.example.nmea.test.NmeaParsingTest;
import org.geotools.feature.SchemaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MainController {

    @Autowired
    NmeaParsingTest nmeaParsingTest;

    @GetMapping("/distancetraveled")
    public String chooseFileMainPage() {
        return "distancetraveled";
    }

    @PostMapping("/distancetraveled")
    public String distanseFromFile(Model model) throws SchemaException, InterruptedException, IllegalAccessException {
        double distance = nmeaParsingTest.fileParsing();
        model.addAttribute("distance", distance);
        return "distancetraveled";
    }
}
