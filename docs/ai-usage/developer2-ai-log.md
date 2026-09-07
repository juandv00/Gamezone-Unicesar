AI Usage Log — Developer 2 (Person Module)

I used Claude throughout the development of my module. These are the things I used it for:

I had a lot of trouble with Git at the beginning: I created my branch with a typo (person-modole instead of person-module), ended up with a branch that had no commit history, and at one point almost committed directly on develop without noticing. Claude helped me diagnose each of these step by step using git status and git branch, and explained why commits directly on main/develop are forbidden by the workshop.
I also switched from IntelliJ to NetBeans midway because I found IntelliJ confusing, and Claude helped me confirm my project was still correctly linked to the Git repository after the switch.
To understand the Person/Client/Seller hierarchy, I asked how super() works in the constructor and why Person needs to be abstract. I initially wrote Person as a normal class and Claude pointed out that it contradicted our own analysis.md, so I corrected it.
For persistence, I got stuck on how to save a list of Person objects to a file when some are actually Client and some are Seller. Claude explained the concept using an unrelated example (animals, not people) — instanceof, casting, and using a type marker at the start of each line — and I used that logic to write my own save() and load() methods for PersonPersistence.

What I DIDN'T do: I didn't ask it to design the hierarchy or decide the attributes (that came from our team's analysis.md), I didn't ask for the analysis answers themselves, and I didn't copy a finished class — I wrote Person, Client, Seller, and PersonPersistence myself, using generic examples as reference.
