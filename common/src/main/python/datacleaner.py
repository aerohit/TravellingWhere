import csv

filtered_rows = []
with open('../resources/capitals.csv', 'rb') as csvfile:
    spamreader = csv.reader(csvfile, delimiter=';', quotechar='"')
    for row in spamreader:
        city = row[0]
        country = row[2]
        country_code = row[4]
        lat = row[6]
        lon = row[7]
        row_key = "/" + country.replace(" ", "-").lower() + "/" + city.replace(" ", "-").lower() + "/"
        filtered_row = [row_key, country, city, country_code, lat, lon]
        print filtered_row
        filtered_rows.append(filtered_row)

with open('../resources/geocoordinates.csv', 'wb') as csvfile:
    spamwriter = csv.writer(csvfile, delimiter='|', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    for row in filtered_rows:
        spamwriter.writerow(row)
