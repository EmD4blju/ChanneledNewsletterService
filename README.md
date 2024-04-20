# Programowanie klient serwer z użyciem gniazd, kanałów i selektorów

## Opis zadania:

- Aplikacja obsługuje rozsyłanie wiadomości do klientów.

- Każdy klient, łącząc się z serwerem, może zapisać się do świadczonej usługi podając tematy, którymi się interesuje (np. polityka, sport, celebryci, gotowanie, randki, ...) jak również zrezygnować ze swoich istniejących tematów.

- Aplikacja umożliwia jednoczesną obsługę wielu klientów.

- Osobny program administruje tematami/wiadomościami i przesyła do serwera wiadomości z różnych dziedzin, a serwer rozsyła je do subskrybentów zainteresowanych danym tematem.

- Program administruje tematami przy pomocy serwera (usuwanie istniejących tematów, dodawanie nowych tematów, informowanie klientów o zmianach dotyczących tematów).

- Do obsługi połączeń (typu "subscribe", "unsubscribe", oraz połączeń przysyłających nowe wiadomości do rozesłania) używane są selektory, nie tworzone są nowe wątki.

- Stworzone jest proste GUI separujące logikę przetwarzania danych.

- Aplikacja jest odporna na różne sytuacje awaryjne.
