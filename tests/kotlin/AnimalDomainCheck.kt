import com.bienestaranimal.zipaquira.animals.AnimalDraft
import com.bienestaranimal.zipaquira.animals.AnimalInputValidator

fun main() {
    val valid = AnimalDraft(
        name = " Luna ",
        species = " Cabra ",
        breed = " Criolla ",
        color = " Blanca ",
        sex = "Hembra",
        approximateAge = "2 años",
        size = "Mediano",
        sterilized = true,
        addressType = "Vereda/Sector",
        municipality = "Zipaquirá",
        neighborhoodOrVillage = "San Jorge",
        addressOrLocation = "Finca El Recuerdo",
        referencePoint = "Portón verde",
    )

    check(AnimalInputValidator.validate(valid) == null)
    check(valid.normalized().name == "Luna")
    check(valid.normalized().species == "Cabra")
    check(AnimalInputValidator.validate(valid.copy(name = "   ")) == "El nombre del animal es obligatorio.")
    check(AnimalInputValidator.validate(valid.copy(species = "")) == "Selecciona la especie del animal.")
    println("ANIMAL_DOMAIN_OK")
}
