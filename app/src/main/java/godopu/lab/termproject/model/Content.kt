package godopu.lab.termproject.model

import godopu.lab.termproject.R

class Content(_title: String, _playTime: Int) {
    val Title = _title
    val EndTime: String =
        if (_playTime%60 < 10)
            "${_playTime / 60}:0${_playTime % 60}"
        else
            "${_playTime / 60}:${_playTime % 60}"

    val PlayTime: Int = _playTime
    val Img: Int
        get() {
            return if (this.Title == "apple carplay") R.mipmap.apple
            else if (this.Title == "google android auto") R.mipmap.google
            else if (this.Title == "volvo_IVI system") R.mipmap.volvo
            else if (this.Title == "samsung_IVI system") R.mipmap.samsung
            else R.mipmap.ic_launcher
        }
}