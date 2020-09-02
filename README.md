# Omsorgsdageroverføringsøknad-api
API-tjeneste for overføring av omsorgsdager(koronadager) og deling av omsorgsdager.

OBS: Tjenesten for overføring av omsorgsdager(koronadager) skal vekk i løpet av kort tid, 
derfor slåss disse tjenestene sammen. 

![CI / CD](https://github.com/navikt/omsorgsdageroverforingsoknad-api/workflows/CI%20/%20CD/badge.svg)

![NAIS Alerts](https://github.com/navikt/omsorgsdageroverforingsoknad-api/workflows/Alerts/badge.svg)

# 1. Kontekst
API for søknad om overføring av omsorgsdager(koronadager) og API for melding om deling av omsorgsdager

# 2. Funksjonelle Krav
Denne tjenesten understøtter søknadsprosessen, samt eksponerer endepunkt for innsending av søknad om overføring av omsorgsdager(koronadager).
Denne tjenesten understøtter søknadsprosessen, samt eksponerer endepunkt for innsending av melding om deling av omsorgsdager.

###Sende inn melding om deling av omsorgsdager
POST @ /melding/dele-dager som gir 202 Accepted

Validering på felter:
- harForståttRettigheterOgPlikter og harBekreftetOpplysninger må være satt til true.
- Kommer mer når dette er avklart....

Eksempel på json
````json
{
                  "språk": "nb",
                  "harForståttRettigheterOgPlikter": true,
                  "harBekreftetOpplysninger": true,
                  "andreBarn": [
                    {
                      "fnr": "12345678900",
                      "ingenFnr": false,
                      "navn": "Barn Barnesen"
                    }
                  ],
                  "harAleneomsorg": true,
                  "harAleneomsorgFor": [
                    {
                      "fødselsdato": "2010-01-01",
                      "fornavn": "Fornavn",
                      "mellomnavn": "Mellomnavn",
                      "etternavn": "Etternavn",
                      "aktørId": "12345"
                    }
                  ],
                  "harUtvidetRett": true,
                  "harUtvidetRettFor": [
                    {
                      "fødselsdato": "2010-01-01",
                      "fornavn": "Fornavn",
                      "mellomnavn": "Mellomnavn",
                      "etternavn": "Etternavn",
                      "aktørId": "12345"
                    }
                  ],
                  "borINorge": true,
                  "arbeidINorge": true,
                  "arbeidssituasjon": [
                    "arbeidstaker"
                  ],
                  "antallDagerHarBruktEtter1Juli": 10,
                  "harDeltDagerMedAndreTidligere": true,
                  "antallDagerHarDeltMedAndre": 10,
                  "overføreTilType": "nyEktefelle",
                  "fnrMottaker": "12345678911",
                  "navnMottaker": "Navn Mottaker",
                  "antallDagerTilOverføre": 5,
                  "harBekreftetMottakerOpplysninger": true
                }
````