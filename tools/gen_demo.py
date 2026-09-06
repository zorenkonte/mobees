#!/usr/bin/env python3
"""Generates app/src/main/assets/demo_titles.json, the bundled sample dataset used when no
TMDB API key is configured. Ratings are illustrative; IMDb ids are real so the OMDb overlay
can fetch actual IMDb ratings in demo mode. Run: python3 tools/gen_demo.py
"""
import json, random, datetime

random.seed(20260906)

def d(s): return datetime.date.fromisoformat(s)

movies = [
  dict(id=155, imdbId="tt0468569", title="The Dark Knight", releaseDate="2008-07-16", rating=8.5, voteCount=33210, runtime=152, trending=True,
       tagline="Welcome to a world without rules.",
       overview="With Gotham's mob on the back foot, Batman, Lieutenant Gordon and District Attorney Harvey Dent form an uneasy alliance. Their progress is upended when a chaotic new criminal calling himself the Joker sets out to prove that everyone can be corrupted.",
       genres=["Action","Crime","Drama"], directors=["Christopher Nolan"],
       cast=[("Christian Bale","Bruce Wayne / Batman"),("Heath Ledger","Joker"),("Aaron Eckhart","Harvey Dent"),("Gary Oldman","James Gordon"),("Maggie Gyllenhaal","Rachel Dawes")],
       franchise=dict(name="The Dark Knight Collection", entries=[(272,"Batman Begins","2005",7.7),(155,"The Dark Knight","2008",8.5),(49026,"The Dark Knight Rises","2012",7.8)]),
       similarIds=[27205, 157336, 680]),
  dict(id=27205, imdbId="tt1375666", title="Inception", releaseDate="2010-07-15", rating=8.4, voteCount=37105, runtime=148, trending=True,
       tagline="Your mind is the scene of the crime.",
       overview="Dom Cobb steals secrets from inside people's dreams. Offered a chance to erase his past, he assembles a team for the reverse task: planting an idea deep in a target's subconscious.",
       genres=["Action","Science Fiction","Adventure"], directors=["Christopher Nolan"],
       cast=[("Leonardo DiCaprio","Dom Cobb"),("Joseph Gordon-Levitt","Arthur"),("Elliot Page","Ariadne"),("Tom Hardy","Eames"),("Cillian Murphy","Robert Fischer")],
       similarIds=[155, 157336, 693134]),
  dict(id=157336, imdbId="tt0816692", title="Interstellar", releaseDate="2014-11-05", rating=8.4, voteCount=36510, runtime=169, trending=True,
       tagline="Mankind was born on Earth. It was never meant to die here.",
       overview="As crops fail across a dying Earth, a former pilot joins a small crew travelling through a wormhole in search of a new home for humanity, leaving his children behind with no promise of return.",
       genres=["Adventure","Drama","Science Fiction"], directors=["Christopher Nolan"],
       cast=[("Matthew McConaughey","Cooper"),("Anne Hathaway","Brand"),("Jessica Chastain","Murph"),("Michael Caine","Professor Brand")],
       similarIds=[27205, 693134, 155]),
  dict(id=238, imdbId="tt0068646", title="The Godfather", releaseDate="1972-03-14", rating=8.7, voteCount=20890, runtime=175,
       tagline="An offer you can't refuse.",
       overview="The ageing patriarch of a New York crime family hands control to his reluctant youngest son, who is drawn ever deeper into a world he wanted no part of.",
       genres=["Drama","Crime"], directors=["Francis Ford Coppola"],
       cast=[("Marlon Brando","Don Vito Corleone"),("Al Pacino","Michael Corleone"),("James Caan","Sonny Corleone"),("Robert Duvall","Tom Hagen"),("Diane Keaton","Kay Adams")],
       franchise=dict(name="The Godfather Collection", entries=[(238,"The Godfather","1972",8.7),(240,"The Godfather Part II","1974",8.6),(242,"The Godfather Part III","1990",7.4)]),
       similarIds=[680, 155]),
  dict(id=680, imdbId="tt0110912", title="Pulp Fiction", releaseDate="1994-09-10", rating=8.5, voteCount=28340, runtime=154,
       tagline="Just because you are a character doesn't mean you have character.",
       overview="Two hit men, a boxer, a gangster's wife and a pair of diner bandits cross paths in a set of interlocking stories told out of order.",
       genres=["Thriller","Crime"], directors=["Quentin Tarantino"],
       cast=[("John Travolta","Vincent Vega"),("Samuel L. Jackson","Jules Winnfield"),("Uma Thurman","Mia Wallace"),("Bruce Willis","Butch Coolidge")],
       similarIds=[238, 155]),
  dict(id=496243, imdbId="tt6751668", title="Parasite", releaseDate="2019-05-30", rating=8.5, voteCount=18920, runtime=133,
       tagline="Act like you own the place.",
       overview="The struggling Kim family talk their way into the household of the wealthy Parks one job at a time, until an unexpected discovery threatens the arrangement.",
       genres=["Comedy","Thriller","Drama"], directors=["Bong Joon-ho"],
       cast=[("Song Kang-ho","Kim Ki-taek"),("Lee Sun-kyun","Park Dong-ik"),("Cho Yeo-jeong","Choi Yeon-gyo"),("Choi Woo-shik","Kim Ki-woo"),("Park So-dam","Kim Ki-jung")],
       similarIds=[680, 129]),
  dict(id=129, imdbId="tt0245429", title="Spirited Away", releaseDate="2001-07-20", rating=8.5, voteCount=16850, runtime=125,
       overview="Ten-year-old Chihiro wanders into a bathhouse for spirits, where a witch turns her parents into pigs. To free them she takes a job under a new name and must not forget her own.",
       genres=["Animation","Family","Fantasy"], directors=["Hayao Miyazaki"],
       cast=[("Rumi Hiiragi","Chihiro Ogino"),("Miyu Irino","Haku"),("Mari Natsuki","Yubaba / Zeniba")],
       similarIds=[862, 496243]),
  dict(id=120, imdbId="tt0120737", title="The Lord of the Rings: The Fellowship of the Ring", releaseDate="2001-12-18", rating=8.4, voteCount=25410, runtime=179,
       tagline="One ring to rule them all.",
       overview="A young hobbit inherits a ring of terrible power and sets out with eight companions to destroy it in the one place it can be unmade.",
       genres=["Adventure","Fantasy","Action"], directors=["Peter Jackson"],
       cast=[("Elijah Wood","Frodo Baggins"),("Ian McKellen","Gandalf"),("Viggo Mortensen","Aragorn"),("Sean Astin","Samwise Gamgee"),("Cate Blanchett","Galadriel")],
       franchise=dict(name="The Lord of the Rings Collection", entries=[(120,"The Fellowship of the Ring","2001",8.4),(121,"The Two Towers","2002",8.4),(122,"The Return of the King","2003",8.5)]),
       similarIds=[693134, 76341]),
  dict(id=76341, imdbId="tt1392190", title="Mad Max: Fury Road", releaseDate="2015-05-13", rating=7.6, voteCount=23110, runtime=121,
       tagline="What a lovely day.",
       overview="In a desert wasteland ruled by a warlord, a drifter and a rebellious lieutenant flee across the sand in an armoured rig with five escapees and an entire war party on their tail.",
       genres=["Action","Adventure","Science Fiction"], directors=["George Miller"],
       cast=[("Tom Hardy","Max Rockatansky"),("Charlize Theron","Imperator Furiosa"),("Nicholas Hoult","Nux"),("Hugh Keays-Byrne","Immortan Joe")],
       franchise=dict(name="Mad Max Collection", entries=[(9659,"Mad Max","1979",6.7),(8810,"Mad Max 2","1981",7.3),(8827,"Beyond Thunderdome","1985",6.3),(76341,"Fury Road","2015",7.6),(786892,"Furiosa","2024",7.5)]),
       similarIds=[693134, 120]),
  dict(id=862, imdbId="tt0114709", title="Toy Story", releaseDate="1995-10-30", rating=8.0, voteCount=18740, runtime=81,
       tagline="The toys are back in town.",
       overview="When a flashy space-ranger toy arrives, a cowboy doll's place as the favourite is threatened. A scheme to get rid of the newcomer strands both of them far from home.",
       genres=["Animation","Adventure","Family","Comedy"], directors=["John Lasseter"],
       cast=[("Tom Hanks","Woody"),("Tim Allen","Buzz Lightyear"),("Don Rickles","Mr. Potato Head")],
       franchise=dict(name="Toy Story Collection", entries=[(862,"Toy Story","1995",8.0),(863,"Toy Story 2","1999",7.6),(10193,"Toy Story 3","2010",7.8),(301528,"Toy Story 4","2019",7.5)]),
       similarIds=[129]),
  dict(id=693134, imdbId="tt15239678", title="Dune: Part Two", releaseDate="2024-02-27", rating=8.2, voteCount=7420, runtime=167, trending=True,
       tagline="Long live the fighters.",
       overview="Paul Atreides joins the Fremen and rides to war against the Harkonnens, while a prophecy he does not trust gathers momentum around him.",
       genres=["Science Fiction","Adventure"], directors=["Denis Villeneuve"],
       cast=[("Timothée Chalamet","Paul Atreides"),("Zendaya","Chani"),("Rebecca Ferguson","Lady Jessica"),("Austin Butler","Feyd-Rautha")],
       franchise=dict(name="Dune Collection", entries=[(438631,"Dune","2021",7.8),(693134,"Dune: Part Two","2024",8.2)]),
       similarIds=[157336, 120, 76341]),
]

def norm_movie(m):
    out = dict(m)
    out["cast"] = [dict(name=n, character=c) for n, c in m["cast"]]
    if "franchise" in m and m["franchise"]:
        out["franchise"] = dict(name=m["franchise"]["name"], entries=[dict(id=i, title=t, year=y, rating=r) for i,t,y,r in m["franchise"]["entries"]])
    return out

# Shows: seasons described as (count, base_rating, start_date, cadence_days, names dict, overrides dict)
def season(num, count, base, start, cadence=7, names=None, overrides=None, votes=900):
    names = names or {}
    overrides = overrides or {}
    eps = []
    for i in range(1, count+1):
        r = overrides.get(i)
        if r is None:
            r = base + random.uniform(-0.3, 0.3)
            if i == count: r += 0.35          # finales tend to land higher
            elif i == 1: r += 0.1
        r = round(min(10.0, max(1.0, r)), 1)
        air = (d(start) + datetime.timedelta(days=cadence*(i-1))).isoformat()
        eps.append(dict(number=i, name=names.get(i, f"Episode {i}"), airDate=air, rating=r,
                        voteCount=int(votes * random.uniform(0.7, 1.3))))
    return dict(number=num, name=f"Season {num}", airDate=start, episodes=eps)

shows = [
  dict(id=1396, imdbId="tt0903747", name="Breaking Bad", firstAirDate="2008-01-20", lastAirDate="2013-09-29", status="Ended", rating=8.9, voteCount=15230, popular=True,
       tagline="Change the equation.",
       overview="A chemistry teacher given a terminal diagnosis turns to cooking methamphetamine with a former student, telling himself it is all for his family.",
       genres=["Drama","Crime"], creators=["Vince Gilligan"], networks=["AMC"], episodeRuntime=47,
       cast=[("Bryan Cranston","Walter White"),("Aaron Paul","Jesse Pinkman"),("Anna Gunn","Skyler White"),("Dean Norris","Hank Schrader"),("Bob Odenkirk","Saul Goodman"),("Giancarlo Esposito","Gus Fring")],
       seasons=[
         season(1, 7, 8.2, "2008-01-20", names={1:"Pilot",2:"Cat's in the Bag...",3:"...And the Bag's in the River",4:"Cancer Man",5:"Gray Matter",6:"Crazy Handful of Nothin'",7:"A No-Rough-Stuff-Type Deal"}),
         season(2, 13, 8.4, "2009-03-08", overrides={12: 9.1, 13: 8.9}),
         season(3, 13, 8.5, "2010-03-21", overrides={12: 9.1, 13: 9.3}),
         season(4, 13, 8.7, "2011-07-17", names={13:"Face Off"}, overrides={11: 9.1, 13: 9.6}),
         season(5, 16, 8.9, "2012-07-15", names={14:"Ozymandias",15:"Granite State",16:"Felina"}, overrides={13: 9.3, 14: 9.9, 15: 9.2, 16: 9.6}),
       ]),
  dict(id=1399, imdbId="tt0944947", name="Game of Thrones", firstAirDate="2011-04-17", lastAirDate="2019-05-19", status="Ended", rating=8.5, voteCount=25890, popular=True,
       tagline="Winter is coming.",
       overview="Noble houses fight for the Iron Throne of Westeros while an ancient threat gathers beyond the Wall in the far north.",
       genres=["Sci-Fi & Fantasy","Drama","Action & Adventure"], creators=["David Benioff","D. B. Weiss"], networks=["HBO"], episodeRuntime=60,
       cast=[("Emilia Clarke","Daenerys Targaryen"),("Kit Harington","Jon Snow"),("Peter Dinklage","Tyrion Lannister"),("Lena Headey","Cersei Lannister"),("Sophie Turner","Sansa Stark"),("Maisie Williams","Arya Stark")],
       seasons=[
         season(1, 10, 8.6, "2011-04-17", overrides={9: 9.3, 10: 9.1}, votes=1600),
         season(2, 10, 8.6, "2012-04-01", overrides={9: 9.4}, votes=1500),
         season(3, 10, 8.7, "2013-03-31", names={9:"The Rains of Castamere"}, overrides={9: 9.6}, votes=1500),
         season(4, 10, 8.9, "2014-04-06", overrides={2: 9.3, 8: 9.4, 10: 9.4}, votes=1500),
         season(5, 10, 8.5, "2015-04-12", names={8:"Hardhome"}, overrides={8: 9.5, 10: 8.8}, votes=1400),
         season(6, 10, 8.8, "2016-04-24", names={9:"Battle of the Bastards",10:"The Winds of Winter"}, overrides={9: 9.7, 10: 9.8}, votes=1500),
         season(7, 7, 8.6, "2017-07-16", overrides={4: 9.3, 6: 8.4, 7: 9.2}, votes=1700),
         season(8, 6, 6.6, "2019-04-14", names={1:"Winterfell",2:"A Knight of the Seven Kingdoms",3:"The Long Night",4:"The Last of the Starks",5:"The Bells",6:"The Iron Throne"},
                overrides={1: 7.4, 2: 7.7, 3: 7.4, 4: 5.5, 5: 5.9, 6: 4.2}, votes=2600),
       ]),
  dict(id=87108, imdbId="tt7366338", name="Chernobyl", firstAirDate="2019-05-06", lastAirDate="2019-06-03", status="Ended", rating=8.7, voteCount=6410, popular=True,
       tagline="What is the cost of lies?",
       overview="A dramatisation of the 1986 nuclear disaster and the scientists, firefighters and miners who tried to contain it while officials worked to contain the truth.",
       genres=["Drama","War & Politics"], creators=["Craig Mazin"], networks=["HBO","Sky Atlantic"], episodeRuntime=62,
       cast=[("Jared Harris","Valery Legasov"),("Stellan Skarsgård","Boris Shcherbina"),("Emily Watson","Ulana Khomyuk"),("Jessie Buckley","Lyudmilla Ignatenko")],
       seasons=[
         season(1, 5, 8.9, "2019-05-06", names={1:"1:23:45",2:"Please Remain Calm",3:"Open Wide, O Earth",4:"The Happiness of All Mankind",5:"Vichnaya Pamyat"},
                overrides={1: 8.6, 2: 8.9, 3: 9.2, 4: 9.0, 5: 9.4}, votes=1200),
       ]),
  dict(id=2316, imdbId="tt0386676", name="The Office", firstAirDate="2005-03-24", lastAirDate="2013-05-16", status="Ended", rating=8.6, voteCount=5120, popular=True,
       overview="A documentary crew follows the staff of a paper company's Scranton branch and the regional manager who is certain he is everybody's best friend.",
       genres=["Comedy"], creators=["Greg Daniels"], networks=["NBC"], episodeRuntime=22,
       cast=[("Steve Carell","Michael Scott"),("Rainn Wilson","Dwight Schrute"),("John Krasinski","Jim Halpert"),("Jenna Fischer","Pam Beesly"),("Mindy Kaling","Kelly Kapoor")],
       seasons=[
         season(1, 6, 7.7, "2005-03-24", votes=420),
         season(2, 22, 8.3, "2005-09-20", overrides={22: 9.2}, votes=400),
         season(3, 25, 8.4, "2006-09-21", overrides={25: 9.1}, votes=380),
         season(4, 19, 8.4, "2007-09-27", overrides={1: 8.9, 19: 8.9}, votes=360),
         season(5, 28, 8.3, "2008-09-25", overrides={14: 9.3, 15: 9.0}, votes=340),
         season(6, 26, 8.0, "2009-09-17", overrides={4: 9.2, 5: 8.9}, votes=300),
         season(7, 26, 8.2, "2010-09-23", names={22:"Goodbye, Michael"}, overrides={22: 9.6, 21: 8.8}, votes=320),
         season(8, 24, 7.3, "2011-09-22", overrides={7: 6.9, 20: 6.8}, votes=260),
         season(9, 25, 7.7, "2012-09-20", names={25:"Finale"}, overrides={23: 8.9, 24: 9.0, 25: 9.6}, votes=300),
       ]),
  dict(id=66732, imdbId="tt4574334", name="Stranger Things", firstAirDate="2016-07-15", lastAirDate="2025-12-31", status="Ended", rating=8.6, voteCount=18900, popular=True,
       tagline="Every ending has a beginning.",
       overview="A boy vanishes from a small Indiana town, and his friends, family and the local police chief uncover a government lab, a girl with strange powers and a world beneath their own.",
       genres=["Drama","Sci-Fi & Fantasy","Mystery"], creators=["Matt Duffer","Ross Duffer"], networks=["Netflix"], episodeRuntime=55,
       cast=[("Millie Bobby Brown","Eleven"),("Finn Wolfhard","Mike Wheeler"),("Winona Ryder","Joyce Byers"),("David Harbour","Jim Hopper"),("Sadie Sink","Max Mayfield")],
       seasons=[
         season(1, 8, 8.5, "2016-07-15", cadence=0, overrides={8: 9.0}, votes=1400),
         season(2, 9, 8.3, "2017-10-27", cadence=0, overrides={7: 6.3, 9: 9.1}, votes=1200),
         season(3, 8, 8.3, "2019-07-04", cadence=0, overrides={8: 9.0}, votes=1100),
         season(4, 9, 8.6, "2022-05-27", cadence=0, names={4:"Dear Billy"}, overrides={4: 9.4, 7: 9.0, 9: 9.3}, votes=1300),
       ]),
  dict(id=95396, imdbId="tt11280740", name="Severance", firstAirDate="2022-02-18", lastAirDate="2025-03-21", status="Returning Series", rating=8.5, voteCount=4880, popular=True,
       tagline="Your outie is well.",
       overview="Employees at a secretive corporation undergo a procedure that splits their work memories from their home lives, until one team starts asking what they actually do all day.",
       genres=["Drama","Mystery","Sci-Fi & Fantasy"], creators=["Dan Erickson"], networks=["Apple TV+"], episodeRuntime=50,
       cast=[("Adam Scott","Mark Scout"),("Britt Lower","Helly R."),("Zach Cherry","Dylan G."),("John Turturro","Irving B."),("Tramell Tillman","Seth Milchick"),("Patricia Arquette","Harmony Cobel")],
       seasons=[
         season(1, 9, 8.4, "2022-02-18", names={9:"The We We Are"}, overrides={8: 8.9, 9: 9.6}, votes=700),
         season(2, 10, 8.5, "2025-01-17", names={7:"Chikhai Bardo",10:"Cold Harbor"}, overrides={7: 9.1, 8: 8.1, 10: 9.5}, votes=650),
       ]),
  dict(id=67070, imdbId="tt5687612", name="Fleabag", firstAirDate="2016-07-21", lastAirDate="2019-04-08", status="Ended", rating=8.3, voteCount=1560,
       overview="A grieving, sharp-tongued Londoner narrates her own life straight to camera while trying to keep a café, a family and a string of bad decisions from falling apart.",
       genres=["Comedy","Drama"], creators=["Phoebe Waller-Bridge"], networks=["BBC Three","Prime Video"], episodeRuntime=27,
       cast=[("Phoebe Waller-Bridge","Fleabag"),("Sian Clifford","Claire"),("Olivia Colman","Godmother"),("Andrew Scott","The Priest")],
       seasons=[
         season(1, 6, 8.1, "2016-07-21", overrides={6: 8.8}, votes=260),
         season(2, 6, 8.7, "2019-03-04", overrides={1: 9.1, 6: 9.5}, votes=320),
       ]),
]

def norm_show(s):
    out = dict(s)
    out["cast"] = [dict(name=n, character=c) for n, c in s["cast"]]
    return out

dataset = dict(movies=[norm_movie(m) for m in movies], shows=[norm_show(s) for s in shows])
import os
path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "assets", "demo_titles.json")
with open(path, "w") as f:
    json.dump(dataset, f, indent=1, ensure_ascii=False)
eps = sum(len(se["episodes"]) for sh in dataset["shows"] for se in sh["seasons"])
print(f"movies={len(dataset['movies'])} shows={len(dataset['shows'])} episodes={eps} bytes={len(json.dumps(dataset))}")
