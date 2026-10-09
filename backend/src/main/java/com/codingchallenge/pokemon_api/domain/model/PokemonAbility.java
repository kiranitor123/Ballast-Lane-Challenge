package com.codingchallenge.pokemon_api.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon_ability")
public class PokemonAbility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Boolean isHidden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_id")
    private Pokemon pokemon;

    public PokemonAbility() {
    }

    public PokemonAbility(Long id, String name, Boolean isHidden, Pokemon pokemon) {
        this.id = id;
        this.name = name;
        this.isHidden = isHidden;
        this.pokemon = pokemon;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private Boolean isHidden;
        private Pokemon pokemon;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder isHidden(Boolean isHidden) {
            this.isHidden = isHidden;
            return this;
        }

        public Builder pokemon(Pokemon pokemon) {
            this.pokemon = pokemon;
            return this;
        }

        public PokemonAbility build() {
            return new PokemonAbility(id, name, isHidden, pokemon);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }
}
