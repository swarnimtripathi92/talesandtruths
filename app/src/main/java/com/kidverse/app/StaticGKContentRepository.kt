package com.kidverse.app

object StaticGKContentRepository {

    data class CategoryContent(
        val intro: String,
        val facts: List<FactItem>
    )

    private val data: Map<String, CategoryContent> = mapOf(
        "our_world" to CategoryContent(
            intro = "Let us travel around our amazing planet and learn simple facts about Earth.",
            facts = listOf(
                FactItem("🌍", "Earth Is Our Home", "Earth is the third planet from the Sun and the only known planet with life.", "https://images.unsplash.com/photo-1614730321146-b6fa6a46bcb4?auto=format&fit=crop&w=900&q=60"),
                FactItem("💧", "Mostly Water", "About 71% of Earth is covered with water, and most of it is in oceans."),
                FactItem("🗺️", "Seven Continents", "Earth has 7 continents: Asia, Africa, North America, South America, Antarctica, Europe, and Australia."),
                FactItem("🌦️", "Different Climates", "Some places are hot, some are cold, and some get lots of rain."),
                FactItem("🌈", "Many Languages", "People around the world speak thousands of different languages."),
                FactItem("🤝", "One Big Family", "Even with many differences, all humans share one planet and must care for it.")
            )
        ),
        "animals" to CategoryContent(
            intro = "Animals and birds are our friends in nature. Let us discover a few fun facts.",
            facts = listOf(
                FactItem("🦒", "Tallest Animal", "The giraffe is the tallest land animal and has a very long neck.", "https://images.unsplash.com/photo-1547721064-da6cfb341d50?auto=format&fit=crop&w=900&q=60"),
                FactItem("🐘", "Strong Memory", "Elephants are known for their strong memory and family bonds."),
                FactItem("🐧", "Bird That Cannot Fly", "Penguins are birds, but they cannot fly. They are excellent swimmers."),
                FactItem("🦉", "Night Flyer", "Owls are active mostly at night and have very sharp hearing."),
                FactItem("🐝", "Busy Bees", "Bees collect nectar from flowers and help plants grow fruits and seeds."),
                FactItem("🐬", "Friendly Dolphins", "Dolphins are smart sea animals and communicate with sounds and clicks.")
            )
        ),
        "plants" to CategoryContent(
            intro = "Plants make Earth green and fresh. They give us oxygen, food, and shade.",
            facts = listOf(
                FactItem("🌱", "Plants Need Sunlight", "Plants use sunlight to make their own food in a process called photosynthesis.", "https://images.unsplash.com/photo-1463936575829-25148e1db1b8?auto=format&fit=crop&w=900&q=60"),
                FactItem("🌳", "Trees Give Oxygen", "Trees absorb carbon dioxide and release oxygen that we breathe."),
                FactItem("🌵", "Desert Plant", "Cactus plants can store water and survive in hot, dry deserts."),
                FactItem("🍎", "Food From Plants", "Many fruits, vegetables, nuts, and grains come from plants."),
                FactItem("🌸", "Flowers Attract Insects", "Bright flowers attract bees and butterflies for pollination."),
                FactItem("🍃", "Leaf Colors", "Most leaves are green because of chlorophyll, but they change colors in some seasons.")
            )
        ),
        "space" to CategoryContent(
            intro = "Space is huge and full of wonders like stars, planets, and moons.",
            facts = listOf(
                FactItem("☀️", "The Sun Is a Star", "The Sun is a star at the center of our solar system.", "https://images.unsplash.com/photo-1614728894747-a83421789f10?auto=format&fit=crop&w=900&q=60"),
                FactItem("🌙", "Earth's Moon", "The Moon is Earth's natural satellite and shines by reflecting sunlight."),
                FactItem("🪐", "Ringed Planet", "Saturn is famous for its beautiful rings made of ice and rock."),
                FactItem("🔴", "The Red Planet", "Mars looks red because of iron-rich dust on its surface."),
                FactItem("🚀", "Astronauts Travel to Space", "Astronauts use rockets and spacecraft to explore space."),
                FactItem("✨", "Twinkling Stars", "Stars seem to twinkle because Earth's atmosphere moves the light.")
            )
        ),
        "india" to CategoryContent(
            intro = "India is a colorful country with rich culture, history, and geography.",
            facts = listOf(
                FactItem("🇮🇳", "National Flag", "India's flag has saffron, white, and green colors with the Ashoka Chakra in the center.", "https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=900&q=60"),
                FactItem("🏛️", "Capital City", "New Delhi is the capital city of India."),
                FactItem("🦚", "National Bird", "The peacock is the national bird of India."),
                FactItem("🐅", "National Animal", "The Bengal tiger is the national animal of India."),
                FactItem("🎉", "Many Festivals", "India celebrates many festivals like Diwali, Holi, Eid, Christmas, and more."),
                FactItem("🗣️", "Many Languages", "India has many languages, and Hindi and English are widely used.")
            )
        ),
        "body" to CategoryContent(
            intro = "Your body is amazing. Every part has a special job to keep you healthy.",
            facts = listOf(
                FactItem("🫀", "Heart Pump", "Your heart pumps blood all around your body.", "https://images.unsplash.com/photo-1530026405186-ed1f139313f8?auto=format&fit=crop&w=900&q=60"),
                FactItem("🧠", "Brain Boss", "The brain controls thinking, movement, memory, and feelings."),
                FactItem("🦴", "Strong Skeleton", "Bones give your body shape and protect important organs."),
                FactItem("👀", "Eyes Help You See", "Your eyes send messages to your brain so you can understand what you see."),
                FactItem("🫁", "Lungs for Breathing", "Lungs take in oxygen and remove carbon dioxide."),
                FactItem("💪", "Muscles Move You", "Muscles help you walk, run, jump, and smile.")
            )
        ),
        "safety" to CategoryContent(
            intro = "Good habits and safety rules keep us healthy, happy, and protected every day.",
            facts = listOf(
                FactItem("🧼", "Wash Hands", "Wash your hands with soap before eating and after using the washroom.", "https://images.unsplash.com/photo-1584744982491-665216d95f8b?auto=format&fit=crop&w=900&q=60"),
                FactItem("⛑️", "Wear a Helmet", "Wear a helmet while cycling or skating to protect your head."),
                FactItem("🚦", "Road Safety", "Cross roads at zebra crossings and follow traffic signals."),
                FactItem("🔥", "Fire Safety", "Never play with fire, matches, or gas stoves without an adult."),
                FactItem("🥗", "Healthy Food", "Eat fruits, vegetables, and drink enough water every day."),
                FactItem("😴", "Sleep Well", "Children need good sleep to stay active and focused.")
            )
        ),
        "colours" to CategoryContent(
            intro = "Colors, shapes, and numbers are all around us and make learning fun.",
            facts = listOf(
                FactItem("🎨", "Primary Colors", "Red, blue, and yellow are primary colors.", "https://images.unsplash.com/photo-1452802447250-470a88ac82bc?auto=format&fit=crop&w=900&q=60"),
                FactItem("🟩", "Common Shapes", "Square, circle, triangle, and rectangle are basic shapes."),
                FactItem("🔢", "Counting Numbers", "Numbers help us count objects like toys, books, and fruits."),
                FactItem("🌈", "Rainbow Colors", "A rainbow has seven colors: violet, indigo, blue, green, yellow, orange, and red."),
                FactItem("🧩", "Shape Hunt", "You can find shapes in daily life, like wheels (circle) and doors (rectangle)."),
                FactItem("➕", "Simple Math", "Adding and subtracting numbers helps in everyday tasks.")
            )
        ),
        "people" to CategoryContent(
            intro = "Different people do different jobs to help society work smoothly.",
            facts = listOf(
                FactItem("👩‍🏫", "Teacher", "Teachers help children learn new things and build good values.", "https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=900&q=60"),
                FactItem("👨‍⚕️", "Doctor", "Doctors treat sick people and guide us to stay healthy."),
                FactItem("👮", "Police Officer", "Police officers keep people safe and maintain law and order."),
                FactItem("👩‍🚒", "Firefighter", "Firefighters rescue people during fires and emergencies."),
                FactItem("👨‍🌾", "Farmer", "Farmers grow crops that become our food."),
                FactItem("👩‍🔬", "Scientist", "Scientists observe, test, and discover new ideas about our world.")
            )
        ),
        "fun" to CategoryContent(
            intro = "Here are some short and surprising facts to enjoy and share with friends.",
            facts = listOf(
                FactItem("🐙", "Three Hearts", "An octopus has three hearts.", "https://images.unsplash.com/photo-1545671913-b89ac1b4ac10?auto=format&fit=crop&w=900&q=60"),
                FactItem("🍯", "Honey Never Spoils", "Honey can stay good for a very long time if stored properly."),
                FactItem("🦋", "Taste With Feet", "Butterflies taste food using their feet."),
                FactItem("🦒", "No Voice Cords", "Giraffes make very little sound and do not have voice cords like humans."),
                FactItem("🌌", "Milky Way", "Our solar system is part of the Milky Way galaxy."),
                FactItem("👂", "Ears and Balance", "Your inner ears also help your body keep balance.")
            )
        )
    )

    fun getCategoryContent(categoryId: String): CategoryContent =
        data[categoryId] ?: CategoryContent(
            intro = "More content coming soon!",
            facts = listOf(FactItem("📘", "Stay Curious", "Keep reading and exploring new topics every day."))
        )
}
