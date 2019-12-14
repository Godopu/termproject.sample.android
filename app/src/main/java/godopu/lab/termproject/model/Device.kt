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
            return if (this._Category == "User") R.mipmap.user
            else if (this._Category == "Car-seat") R.mipmap.ic_car_seat
            else if (this._Category == "Display") R.mipmap.display
            else if (this._Category == "Speaker") R.mipmap.speaker
            else if (this._Category == "Sensor") R.mipmap.sensor
            else if (this._Category == "Air-Conditioner") R.mipmap.aircon
            else if (this._Category == "Heated-Seat") R.mipmap.heatedseat
            else R.mipmap.ic_launcher
        }
}
