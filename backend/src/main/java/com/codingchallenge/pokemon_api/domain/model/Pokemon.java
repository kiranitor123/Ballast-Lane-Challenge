package com.codingchallenge.pokemon_api.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pokemon")
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer pokeApiId;

    @Column(nullable = false)
    private String name;

    private Integer weight;
    private Integer height;

    @Column(length = 512)
    private String spriteUrl;

    private String types;

    private Integer hp;
    private Integer attack;
    private Integer defense;
    private Integer specialAttack;
    private Integer specialDefense;
    private Integer speed;

    private String localName;
    private String region;
    private String customTag;

    private Boolean synced = false;
    private LocalDateTime syncedAt;

    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PokemonAbility> abilities = new ArrayList<>();

    public Pokemon() {
    }

    public Pokemon(Long id, Integer pokeApiId, String name, Integer weight, Integer height,
                   String spriteUrl, String types, Integer hp, Integer attack, Integer defense,
                   Integer specialAttack, Integer specialDefense, Integer speed,
                   String localName, String region, String customTag,
                   Boolean synced, LocalDateTime syncedAt, List<PokemonAbility> abilities) {
        this.id = id;
        this.pokeApiId = pokeApiId;
        this.name = name;
        this.weight = weight;
        this.height = height;
        this.spriteUrl = spriteUrl;
        this.types = types;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.localName = localName;
        this.region = region;
        this.customTag = customTag;
        this.synced = synced != null ? synced : false;
        this.syncedAt = syncedAt;
        this.abilities = abilities != null ? abilities : new ArrayList<>();
    }

    public void addAbility(PokemonAbility ability) {
        abilities.add(ability);
        ability.setPokemon(this);
    }

    public void clearAbilities() {
        for (PokemonAbility ability : abilities) {
            ability.setPokemon(null);
        }
        abilities.clear();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Integer pokeApiId;
        private String name;
        private Integer weight;
        private Integer height;
        private String spriteUrl;
        private String types;
        private Integer hp;
        private Integer attack;
        private Integer defense;
        private Integer specialAttack;
        private Integer specialDefense;
        private Integer speed;
        private String localName;
        private String region;
        private String customTag;
        private Boolean synced = false;
        private LocalDateTime syncedAt;
        private List<PokemonAbility> abilities = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder pokeApiId(Integer pokeApiId) {
            this.pokeApiId = pokeApiId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder weight(Integer weight) {
            this.weight = weight;
            return this;
        }

        public Builder height(Integer height) {
            this.height = height;
            return this;
        }

        public Builder spriteUrl(String spriteUrl) {
            this.spriteUrl = spriteUrl;
            return this;
        }

        public Builder types(String types) {
            this.types = types;
            return this;
        }

        public Builder hp(Integer hp) {
            this.hp = hp;
            return this;
        }

        public Builder attack(Integer attack) {
            this.attack = attack;
            return this;
        }

        public Builder defense(Integer defense) {
            this.defense = defense;
            return this;
        }

        public Builder specialAttack(Integer specialAttack) {
            this.specialAttack = specialAttack;
            return this;
        }

        public Builder specialDefense(Integer specialDefense) {
            this.specialDefense = specialDefense;
            return this;
        }

        public Builder speed(Integer speed) {
            this.speed = speed;
            return this;
        }

        public Builder localName(String localName) {
            this.localName = localName;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder customTag(String customTag) {
            this.customTag = customTag;
            return this;
        }

        public Builder synced(Boolean synced) {
            this.synced = synced;
            return this;
        }

        public Builder syncedAt(LocalDateTime syncedAt) {
            this.syncedAt = syncedAt;
            return this;
        }

        public Builder abilities(List<PokemonAbility> abilities) {
            this.abilities = abilities;
            return this;
        }

        public Pokemon build() {
            return new Pokemon(id, pokeApiId, name, weight, height, spriteUrl, types,
                    hp, attack, defense, specialAttack, specialDefense, speed,
                    localName, region, customTag, synced, syncedAt, abilities);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPokeApiId() {
        return pokeApiId;
    }

    public void setPokeApiId(Integer pokeApiId) {
        this.pokeApiId = pokeApiId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getSpriteUrl() {
        return spriteUrl;
    }

    public void setSpriteUrl(String spriteUrl) {
        this.spriteUrl = spriteUrl;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Integer getAttack() {
        return attack;
    }

    public void setAttack(Integer attack) {
        this.attack = attack;
    }

    public Integer getDefense() {
        return defense;
    }

    public void setDefense(Integer defense) {
        this.defense = defense;
    }

    public Integer getSpecialAttack() {
        return specialAttack;
    }

    public void setSpecialAttack(Integer specialAttack) {
        this.specialAttack = specialAttack;
    }

    public Integer getSpecialDefense() {
        return specialDefense;
    }

    public void setSpecialDefense(Integer specialDefense) {
        this.specialDefense = specialDefense;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCustomTag() {
        return customTag;
    }

    public void setCustomTag(String customTag) {
        this.customTag = customTag;
    }

    public Boolean getSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
    }

    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }

    public List<PokemonAbility> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<PokemonAbility> abilities) {
        this.abilities = abilities;
    }
}
