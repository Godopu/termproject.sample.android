package godopu.lab.termproject.model

import godopu.lab.termproject.R

class Device(private var _Name: String , private var _Category : String) {
    var Name
        get() = _Name
        set(value) {
            _Name = value
        }

    var Category
        get() = _Category
        set(value) {
            _Category = value
        }

    val Image: Int
        get() {
            return if (this._Category == "LED") R.mipmap.led
            else if (this._Category == "Display") R.mipmap.display
            else R.mipmap.ic_launcher
        }
}
