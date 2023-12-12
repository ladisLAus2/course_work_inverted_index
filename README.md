Інструкція збору та запуску проєкту
0. Завантаження та встановлення Git
1. Відкриття терміналу та клонування проекту на ваш пристрій ```git clone https://github.com/ladisLAus2/course_work_inverted_index.git```
2. Перехід в головну директорію проекту та створюємо директорію для скомпільованого проекту ```cd course_work_inverted_index/ && mkdir out```(для Windows)
3. Компіляція коду ```javac -d out src/*.java src/components/*.java src/components/concurrentMap/*.java```
4. Виконуємо програму сервера ```java -cp out Server```
5. Виконуємо програму клієнта, попередньо перейшовши до директорії ```course_work_inverted_index``` в іншому терміналі та вводимо команду ```java -cp out Client```
