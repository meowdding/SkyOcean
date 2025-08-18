
import me.owdding.skyblockpv.museum.CreateMuseumDataTask

val museumDataTask = tasks.register<CreateMuseumDataTask>("createMuseumData")

tasks.withType<ProcessResources>().configureEach {
    dependsOn(museumDataTask.get())
    from(museumDataTask.get().outputs.files)
}
