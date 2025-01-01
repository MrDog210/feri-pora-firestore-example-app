# Android Firestore Example app
[Android firebase sdk github link](https://github.com/firebase/firebase-android-sdk)  
## Zakaj firestore?
Firestore je odlična izbira za:
   - Aplikacije, ki potrebujejo sprotno sinhronizacijo podatkov med napravami.
   - Programe, ki morajo delovati tudi brez internetne povezave.
   - Projekte, kjer je potreben hiter razvoj brez potrebe po postavitvi in vzdrževanju lastnih strežnikov.

Firestore je idealen za razvijalce, ki iščejo rešitev z minimalnim upravljanjem infrastrukture, 
saj omogoča osredotočenje na razvoj aplikacij brez skrbi glede strežniške infrastrukture in skalabilnosti.  

## Prednosti
   - Sprotna sinhronizacija podatkov (realno časovne posodobitve)
   - Podpora delovanje brez internetne povezave, Spremembe se sinhronizirajo samodejno, ko se naprava ponovno poveže
   - Skalabilnost
   - Enostavna integracija
   - Varnost in pravice dostopa, točno lahko omogočamo dostop, do podatkov v firestore Security Rules
   - Veliko primerov in podpore, saj projekt vzdržuje google
   - Zastonj za majhne aplikacije, z malo uporabniki
   - Nimamo infrastrukture za vzdrževati

## Slabosti
   - Stroški pri večjih projektih
   - Omejitve poizvedb (prei veliki uporabi indexov in JOIN-ov)
   - Odvisnost od Googlove infrastrukture

## Število zvezdic, sledilcev, forkov
   - zvezdic: 2.3k
   - sledilcev: 173
   - forkov: 584

## Vzdrževanje projekta
Projekt je redno vzdrževan, nazadnje je bil posodobljen pret petimi dnevi.

# Primer uporabe

![](readme/eaxmple.mp4)

```kt
data class TaskItem (
    var name: String = "",
    var id: String = UUID.randomUUID().toString()
)
```

```kt
class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore // Inicializacija Firestore instance za dostop do baze podatkov.
    private lateinit var taskList: ArrayList<TaskItem> // Seznam za shranjevanje podatkov o opravilih.
    private var taskListener: ListenerRegistration? = null // Poslušalec za realnočasovne spremembe v zbirki podatkov.

    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        db = FirebaseFirestore.getInstance() // Pridobitev instance Firestore baze.
        ...
        taskList = ArrayList() // Inicializacija praznega seznama opravil.

        // Adapter za prikaz seznama opravil in upravljanje z odstranjevanjem.
        adapter = TaskAdapter(taskList!!, object: TaskAdapter.TaskRemoveListener {
            override fun onRemove(taskId: String) {
                db.collection("tasks").document(taskId)
                    .delete() // Izbris dokumenta iz zbirke "tasks" glede na ID.
                    .addOnCompleteListener { task: Task<Void?> ->
                        if (!task.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Failed to delete task", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        })

        // Poslušalec za sprotno spremljanje sprememb v zbirki "tasks".
        taskListener = db.collection("tasks")
            .addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Toast.makeText(this@MainActivity, "Error fetching tasks", Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                taskList.clear() // Počisti lokalni seznam opravil.
                for (doc in snapshots!!) { // Iteracija čez dokumente v zbirki.
                    val task = doc.toObject(TaskItem::class.java) // Pretvorba dokumenta v objekt TaskItem.
                    task.id = doc.id // Nastavitev ID-ja za nalogo.
                    taskList.add(task) // Dodajanje naloge v lokalni seznam.
                }
                adapter.notifyDataSetChanged() // Obveščanje adapterja, da so podatki posodobljeni.
            }
    }

    // Funkcija za dodajanje naloge v bazo.
    private fun addTask(view: View) {
        val taskText = taskInput!!.text.toString() // Prebere vnos besedila uporabnika.
        if (TextUtils.isEmpty(taskText)) { // Preveri, ali je vnos prazen.
            Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val task: MutableMap<String, Any> = HashMap() // Ustvari slovar za podatke naloge.
        task["name"] = taskText // Dodajanje imena naloge.

        db.collection("tasks")
            .add(task) // Dodajanje naloge v zbirko "tasks".
            .addOnCompleteListener { task: Task<DocumentReference?> ->
                if (task.isSuccessful) {
                    taskInput!!.setText("") // Po uspešnem vnosu počisti vnosno polje.
                } else {
                    Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Poskrbi za ustrezno čiščenje virov ob uničenju aktivnosti.
    override fun onDestroy() {
        super.onDestroy()
        taskListener?.remove() // Odstranitev poslušalca za preprečitev puščanja virov.
    }
}
```