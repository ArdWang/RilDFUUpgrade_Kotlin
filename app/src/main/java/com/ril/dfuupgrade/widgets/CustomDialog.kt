package com.ril.dfuupgrade.widgets

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.TextView
import com.ril.dfuupgrade.R


class CustomDialog(context: Context, theme: Int) : Dialog(context, theme) {

    /**
     * Helper class for creating a custom dialog
     */
    class Builder(private val context: Context) {
        private var title: String? = null
        private var message: String? = null
        private var positiveButtonText: String? = null
        private var negativeButtonText: String? = null
        private var contentView: View? = null

        private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
        private var negativeButtonClickListener: DialogInterface.OnClickListener? = null

        /**
         * Set the Dialog message from String
         * //@param title
         * @return
         */
        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        /**
         * Set the Dialog message from resource
         * //@param title
         * @return
         */
        fun setMessage(message: Int): Builder {
            this.message = context.getText(message) as String
            return this
        }

        /**
         * Set the Dialog title from resource
         * @param title
         * @return
         */
        fun setTitle(title: Int): Builder {
            this.title = context.getText(title) as String
            return this
        }

        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         * @param v
         * @return
         */
        fun setContentView(v: View): Builder {
            this.contentView = v
            return this
        }

        /**
         * Set the positive button resource and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        fun setPositiveButton(positiveButtonText: Int, listener: DialogInterface.OnClickListener): Builder {
            this.positiveButtonText = context.getText(positiveButtonText) as String
            this.positiveButtonClickListener = listener
            return this
        }

        /**
         * Set the positive button text and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        fun setPositiveButton(positiveButtonText: String, listener: DialogInterface.OnClickListener): Builder {
            this.positiveButtonText = positiveButtonText
            this.positiveButtonClickListener = listener
            return this
        }

        /**
         * Set the negative button resource and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        fun setNegativeButton(negativeButtonText: Int, listener: DialogInterface.OnClickListener): Builder {
            this.negativeButtonText = context.getText(negativeButtonText) as String
            this.negativeButtonClickListener = listener
            return this
        }


        /**
         * Set the negative button text and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        fun setNegativeButton(negativeButtonText: String, listener: DialogInterface.OnClickListener): Builder {
            this.negativeButtonText = negativeButtonText
            this.negativeButtonClickListener = listener
            return this
        }

        /**
         * Create the custom dialog
         */
        fun create(): CustomDialog {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            // instantiate the dialog with the custom Theme
            val dialog = CustomDialog(context, R.style.Dialog)
            dialog.setCanceledOnTouchOutside(false)
            val layout = inflater.inflate(R.layout.custom_dialog_layout, null)
            //layout.getBackground().setAlpha(100);
            dialog.addContentView(layout, LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT))
            // set the dialog title
            (layout.findViewById<View>(R.id.mTips) as TextView).text = title
            // set the dialog content
            (layout.findViewById<View>(R.id.mContent) as TextView).text = message
            // set the confirm button
            if (positiveButtonText != null) {
                (layout.findViewById<View>(R.id.positiveButton) as Button).text = positiveButtonText
                if (positiveButtonClickListener != null) {
                    layout.findViewById<View>(R.id.positiveButton).setOnClickListener {
                        positiveButtonClickListener!!.onClick(
                            dialog,
                            DialogInterface.BUTTON_POSITIVE
                        )
                    }
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById<View>(R.id.positiveButton).visibility = View.GONE
            }

            // set the cancel button
            if (negativeButtonText != null) {
                (layout.findViewById<View>(R.id.negativeButton) as Button).text = negativeButtonText
                if (negativeButtonClickListener != null) {
                    layout.findViewById<View>(R.id.negativeButton).setOnClickListener {
                        negativeButtonClickListener!!.onClick(
                            dialog,
                            DialogInterface.BUTTON_NEGATIVE
                        )
                    }
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById<View>(R.id.negativeButton).visibility = View.GONE
            }


            dialog.setContentView(layout)
            return dialog
        }

    }


}
