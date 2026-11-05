package dev.che1s.syalsdelight.compat;

import java.util.List;

public record OptionalDependency(String itemId, String usedBy) {
    public static final List<OptionalDependency> ITEMS = List.of(
            new OptionalDependency("gtceu:tiny_naquadria_dust", "fried_naquadah_potato_slice"),
            new OptionalDependency(
                    "gtceu:tiny_uranium_rhodium_dinaquadide_dust",
                    "fried_naquadah_potato_slice"
            ),
            new OptionalDependency("gtceu:tiny_naquadah_alloy_dust", "fried_naquadah_potato_slice"),
            new OptionalDependency("gtceu:tiny_naquadah_dust", "fried_naquadah_potato_slice"),
            new OptionalDependency(
                    "gtceu:tiny_enriched_naquadah_dust",
                    "fried_naquadah_potato_slice"
            ),
            new OptionalDependency("mekanism_extras:dust_naquadah", "fried_naquadah_potato_slice"),
            new OptionalDependency("ltc2:solid_underground_oil", "underground_oil_bottle"),
            new OptionalDependency("unusualfoodsdelight:zombie_oil_block", "zombie_oil_bottle"),
            new OptionalDependency("vintagedelight:vinegar_bottle", "vinegar_chips"),
            new OptionalDependency("manors_bounty:rice_vinegar", "vinegar_chips"),
            new OptionalDependency("dumplings_delight:vinegar", "vinegar_chips"),
            new OptionalDependency("smc:star_dust", "astera_chips"),
            new OptionalDependency("smc:broccoli", "broccoli_flavored_slimming_chips"),
            new OptionalDependency("kaleidoscope_cookery:donkey_burger", "donkey_burger_chips"),
            new OptionalDependency("kaleidoscope_cookery:oil", "kale_oil"),
            new OptionalDependency("cosmopolitan:green_sauce", "pesto_flavored_chips")
    );
}

