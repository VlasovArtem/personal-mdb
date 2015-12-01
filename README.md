# Series IMDB parser
Also it works as api for parsed series from website IMDB
Parse series by title with full information about series, season count and information about episodes in each season, data of the next episode, cast, creators and etc.
## Possible requests
* /api/v1/series?title=:title - search series by **title**.
* /api/v1/series/short/year/:year - collect all series with specify **year**.
* /api/v1/series/short/genre/:genre - collect all series with specify **genre**.
Search by year and genre specify additional request params:
* sort=:sort - collect series with specify data and sort with **sort** data, sort has 3 posible sorting properties:
 * rating - sort by IMDB rating
 * year - sort by series year
 * title - sort by series title
* order=:order - collect series with specify data and sort by propety and paticular order (default: **DESC**). Can be used only with sort parametr
 * ASC - ascending order
 * DESC - descending order
* hideFinished=:boolean - collect series that is not finished (default: false).
Example:
* /api/v1/series?title=Grimm - find information about series with title Grimm
* /api/v1/series/year/2015?sort=rating - collect all series with year start 2015 with sorting property rating in desceding order.
* /api/v1/series/genre/Action?hideFinished=true - collect all series with Genre Action and hide finished.
