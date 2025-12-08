package com.polypadel.matches.dto;

import java.util.List;

public class TeamDto {
    public String company;
    public List<PlayerDto> players;

    public TeamDto(String company, List<PlayerDto> players) {
        this.company = company;
        this.players = players;
    }

    public static class PlayerDto {
        public String first_name; // Snake_case pour matcher le frontend
        public String last_name;

        public PlayerDto(String first, String last) {
            this.first_name = first;
            this.last_name = last;
        }
    }
}