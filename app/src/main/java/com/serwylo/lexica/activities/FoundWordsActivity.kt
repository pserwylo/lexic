package com.serwylo.lexica.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.serwylo.lexica.ThemeManager
import com.serwylo.lexica.Util
import com.serwylo.lexica.activities.score.WordDefiner
import com.serwylo.lexica.databinding.FoundWordBinding
import com.serwylo.lexica.databinding.FoundWordsBinding
import com.serwylo.lexica.db.*
import com.serwylo.lexica.lang.Language
import com.serwylo.lexica.lang.LanguageLabel
import com.serwylo.lexica.view.CustomTextArrayAdapter

class FoundWordsActivity : AppCompatActivity() {

    private lateinit var binding: FoundWordsBinding
    private lateinit var selectedLanguage: Language

    private val languages = Language.allLanguages.values.toList()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        ThemeManager.getInstance().applyTheme(this)
        selectedLanguage = Util().getSelectedLanguageOrDefault(this)

        binding = FoundWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.foundWordsList.layoutManager = GridLayoutManager(this, calcNumColumns())
        binding.foundWordsList.setHasFixedSize(true)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.language.adapter = CustomTextArrayAdapter(this, languages) { LanguageLabel.getLabel(this, it) }
        binding.language.setSelection(languages.indexOf(selectedLanguage))

        binding.language.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(av: AdapterView<*>?, view: View?, index: Int, id: Long) {
                selectedLanguage = languages[index]
                loadWords()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun calcNumColumns(): Int {
        val widthDp = resources.displayMetrics.run { widthPixels / density }

        return (widthDp / 100).toInt()
    }

    private fun loadWords() {
        val repo = SelectedWordRepository(applicationContext)

        repo.findAllWordsByLanguage(selectedLanguage).observe(this, { words ->
            binding.foundWordsList.adapter = WordsAdapter(words.map { it.word })
        })
    }

    inner class WordsAdapter(val words: List<String>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = FoundWordBinding.inflate(this@FoundWordsActivity.layoutInflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(words[position])
        }

        override fun getItemCount(): Int {
            return words.size
        }

    }

    inner class ViewHolder(private val binding: FoundWordBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(word: String) {
            binding.text1.text = word
            binding.text1.setOnLongClickListener { WordDefiner(this@FoundWordsActivity, selectedLanguage).define(binding.text1.text.toString()) }
        }

    }

}