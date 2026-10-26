# Tabibi — application Android de gestion de cabinet médical

Application Android native (Java) qui met en relation patients et médecins :
création et gestion d'un cabinet, prise de rendez-vous, suivi des demandes par le médecin.
Les données sont stockées localement en SQLite.

## Fonctionnalités

- **Comptes** : inscription et connexion, mots de passe hachés (`PasswordHasher`), session persistante (`SessionManager`)
- **Médecin** : tableau de bord, profil, création et gestion de son cabinet, agenda,
  liste des patients, validation des rendez-vous en attente
- **Patient** : consultation des cabinets, prise de rendez-vous, suivi de ses rendez-vous
- **Rendez-vous** : création, détail, listes filtrées (en attente / tous / par patient)

## Stack

Java · Android SDK (minSdk 24, targetSdk 36) · SQLite (`SQLiteOpenHelper`) ·
RecyclerView · Gradle Kotlin DSL

## Structure

```
app/src/main/java/com/example/tp5ex2/
  MainActivity, activity_register          connexion et inscription
  Doctor*Activity                          espace médecin
  CreateCabinetActivity, ManageCabinetActivity, CabinetDetailActivity
  AddAppointmentActivity, AppointmentActivity, AppointmentDetailActivity
  *Adapter                                 listes RecyclerView
  DatabaseHelper, UserDatabaseHelper       schéma SQLite (users, medical_cabinets, appointments)
```

## Lancer

1. Ouvrir le dossier dans Android Studio.
2. Laisser Gradle synchroniser (le fichier `local.properties` est recréé automatiquement).
3. Lancer sur un émulateur ou un téléphone (Android 7.0+).
