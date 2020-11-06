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

### Sende inn melding om deling av omsorgsdager
POST @ /melding/dele-dager som gir 202 Accepted

Validering på felter:
- harForståttRettigheterOgPlikter og harBekreftetOpplysninger må være satt til true.
- antallDagerSomSkalOverføres må være mellom 1-10
- mottakerFnr må være gyldig
- Barn blir sendt inn både med og uten identitetsnummer. Vi slår opp på nytt og
    populerer de som mangler identitetsnummer. Validering på at identitetsnummer skal være gyldig.
- Alle bolske verdier blir satt til null slik at vi unngår default false. Vi validerer at de har blitt satt til true eller false

Eksempel på json
````json
{
  "språk": "nb",
  "id": "1",
  "harForståttRettigheterOgPlikter": true,
  "harBekreftetOpplysninger": true,
  "barn": [
    {
      "identitetsnummer": null,
      "aktørId": "1000000000001",
      "navn": "Barn Barnesen",
      "fødselsdato": "2020-01-01",
      "aleneOmOmsorgen": true,
      "utvidetRett": true
    },
    {
      "identitetsnummer": "07068920285",
      "aktørId": null,
      "navn": "Barn Barnesen",
      "fødselsdato": "2020-01-01",
      "aleneOmOmsorgen": true,
      "utvidetRett": true
    }
  ],
  "borINorge": true,
  "arbeiderINorge": true,
  "arbeidssituasjon": [
    "arbeidstaker"
  ],
  "antallDagerBruktEtter1Juli": 10,
  "mottakerType": "ektefelle",
  "mottakerFnr": "07068920285",
  "mottakerNavn": "Navn Mottaker",
  "antallDagerSomSkalOverføres": 5
}
````
