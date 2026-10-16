package com.pookie.game.domain.engine

import com.pookie.game.domain.model.SoulSyncPrompt

object SoulSyncPromptCatalog {

    val CATALOG: List<String> = listOf(
        "What is {TARGET}'s absolute favorite comfort food when having a rough day?",
        "What was the very first impression {TARGET} had of you?",
        "If {TARGET} could immediately master any musical instrument, which one would it be?",
        "What is {TARGET}'s biggest guilty pleasure song or artist?",
        "If {TARGET} had 1 million dollars to spend in 24 hours, what would they buy first?",
        "What is {TARGET}'s go-to midnight snack when everyone is asleep?",
        "What is one dream destination {TARGET} wants to visit with you?",
        "What silly quirk or habit of {TARGET} always makes you smile?",
        "What was {TARGET}'s childhood dream career or aspiration?",
        "What is {TARGET}'s favorite way to unwind after a stressful day?",
        "Which fictional character reminds you most of {TARGET}?",
        "What is {TARGET}'s favorite physical feature of yours?",
        "What is something that never fails to make {TARGET} laugh out loud?",
        "What was {TARGET}'s favorite subject back in school?",
        "If {TARGET} could have any superpower, what would they choose?",
        "What is {TARGET}'s favorite season of the year and why?",
        "What is {TARGET}'s biggest pet peeve in daily life?",
        "If {TARGET} could only eat one meal for the rest of their life, what would it be?",
        "What is the most adventurous thing {TARGET} has ever done?",
        "What is {TARGET}'s favorite coffee, tea, or signature drink?",
        "What movie could {TARGET} rewatch a hundred times without getting bored?",
        "What is {TARGET}'s hidden or underrated talent?",
        "Who is {TARGET}'s ultimate celebrity crush?",
        "What was {TARGET}'s first ever job or gig?",
        "If {TARGET} were an animal, what animal would they be?",
        "What is {TARGET}'s favorite holiday or celebration?",
        "What is {TARGET}'s primary love language (Words, Touch, Gifts, Acts, Quality Time)?",
        "What scent or perfume immediately reminds you of {TARGET}?",
        "What is {TARGET}'s favorite thing about your relationship?",
        "If {TARGET} could time travel, would they visit the past or future?",
        "What is {TARGET}'s favorite board or video game of all time?",
        "What is one fear or phobia that {TARGET} secretly has?",
        "What song always gets {TARGET} singing in the car or shower?",
        "What is {TARGET}'s favorite dessert or sweet treat?",
        "If {TARGET} could teleport anywhere right now for 1 hour, where would they go?",
        "What is {TARGET}'s favorite clothing item or favorite outfit to wear?",
        "How does {TARGET} like their eggs cooked for breakfast?",
        "What is {TARGET}'s dream car or dream way of traveling?",
        "What is one item {TARGET} cannot leave the house without?",
        "What was {TARGET}'s favorite cartoon or TV show growing up?",
        "What is {TARGET}'s ideal temperature when sleeping (Freezing cold or Warm & toasty)?",
        "What is {TARGET}'s favorite pizza topping combination?",
        "If {TARGET} could adopt any wild exotic animal as a pet, what would it be?",
        "What is the sweetest thing {TARGET} has ever done for you?",
        "How does {TARGET} handle scary movies or haunted houses?",
        "What is {TARGET}'s most frequently used emoji in text messages?",
        "If {TARGET} were stranded on a deserted island, what 1 item would they bring?",
        "What is {TARGET}'s proudest personal accomplishment so far?",
        "What is {TARGET}'s favorite ice cream flavor?",
        "What would {TARGET} name a cute little puppy if you got one together today?",
        "What is {TARGET}'s favorite indoor hobby on a rainy Sunday afternoon?",
        "Does {TARGET} prefer sunrise mornings or late-night stargazing?",
        "What is {TARGET}'s go-to karaoke anthem when handed a microphone?",
        "What is the funniest nickname {TARGET} has ever had?",
        "If {TARGET} opened a restaurant or café, what theme or cuisine would it be?",
        "What was the very first gift {TARGET} ever gave you?",
        "What is {TARGET}'s favorite kind of flowers or plants?",
        "If {TARGET} could meet any historical figure for dinner, who would it be?",
        "What is {TARGET}'s favorite theme park ride (Coaster, Water ride, or Ferris wheel)?",
        "What is {TARGET}'s ideal date night vibe (Fancy dinner or Cozy couch takeout)?",
        "What is one language {TARGET} has always wanted to speak fluently?",
        "What is {TARGET}'s favorite memory of the two of you laughing together?",
        "What is {TARGET}'s biggest weakness while shopping at a supermarket?",
        "If {TARGET} wrote a book, what genre would it belong to?",
        "What is {TARGET}'s favorite sport to play or watch?",
        "What is the most thoughtful compliment {TARGET} ever received?",
        "How does {TARGET} take their morning beverage (Sweet, Black, Milk, Iced)?",
        "What is {TARGET}'s favorite spot in the house to relax?",
        "If {TARGET} could trade lives with anyone for 24 hours, who would it be?",
        "What is {TARGET}'s signature kitchen dish that they make best?",
        "What was the last thing that made {TARGET} shed happy tears?",
        "What is {TARGET}'s favorite party game to play with friends?",
        "What is one bucket list item {TARGET} talks about constantly?",
        "Does {TARGET} prefer the mountains, the beach, or a bustling city?",
        "What is {TARGET}'s favorite shoe or sneaker style?",
        "If {TARGET} could design their dream home, what must-have room would it include?",
        "What is {TARGET}'s favorite comedy movie of all time?",
        "What is {TARGET}'s phone battery percentage usually at (90%+ or living at 5%)?",
        "What is {TARGET}'s favorite thing you do when they are feeling down?",
        "If {TARGET} had to survive a zombie apocalypse, what would their weapon of choice be?",
        "What is {TARGET}'s favorite smell in the world (Fresh rain, Coffee, Baked cookies)?",
        "What is {TARGET}'s favorite breakfast food?",
        "If {TARGET} could star in a TV show, which existing show would they join?",
        "What is {TARGET}'s favorite way to receive affection (Hug, Kiss, Back rub, Holding hands)?",
        "What is {TARGET}'s honest opinion on pineapple on pizza?",
        "What is {TARGET}'s favorite game to play when traveling?",
        "What is the first concert or live show {TARGET} ever attended?",
        "If {TARGET} were given a free plane ticket right now, what city would they fly to?",
        "What is {TARGET}'s favorite quote, motto, or personal life philosophy?",
        "What is {TARGET}'s favorite childhood memory?",
        "What was {TARGET}'s favorite bedtime story or book growing up?",
        "What is {TARGET}'s favorite jacket, hoodie, or sweater to wear?",
        "What is {TARGET}'s favorite late-night TV show or YouTube channel to binge?",
        "What is {TARGET}'s favorite flavor of bubble tea, smoothie, or juice?",
        "What is {TARGET}'s favorite kind of massage or pampering?",
        "What was {TARGET}'s very first car or mode of transport?",
        "What is {TARGET}'s favorite thing about your smile?",
        "If {TARGET} could live anywhere in the world for 1 year, where would it be?",
        "What is {TARGET}'s absolute favorite romantic song about us?",
        "What is one promise you know {TARGET} will always keep?"
    )

    fun generateSessionPrompts(totalRounds: Int = 10): List<SoulSyncPrompt> {
        val shuffled = CATALOG.shuffled()
        val count = totalRounds.coerceIn(1, shuffled.size)
        return shuffled.take(count).mapIndexed { index, template ->
            val role = if (index % 2 == 0) {
                if (Math.random() < 0.5) "HOST" else "PARTNER"
            } else {
                if (Math.random() < 0.5) "PARTNER" else "HOST"
            }
            SoulSyncPrompt(
                id = "SS_${index + 1}",
                targetRole = role,
                template = template
            )
        }
    }
}
