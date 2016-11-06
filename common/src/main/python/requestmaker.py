import csv, time, random
import urllib2

all_lines = []

with open('../resources/geocoordinates.csv', 'rb') as csvfile:
    spamreader = csv.reader(csvfile, delimiter='|', quotechar='"')
    for row in spamreader:
        all_lines.append(row)

while(True):
    row_no = random.randint(0, len(all_lines) - 1)
    url = "http://localhost:9000/places" + all_lines[row_no][0]
    print url
    urllib2.urlopen(url).read()
    time.sleep(1)
