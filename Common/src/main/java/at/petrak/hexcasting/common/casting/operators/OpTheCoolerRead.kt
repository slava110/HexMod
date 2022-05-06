package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.item.ItemEntity

object OpTheCoolerRead : ConstManaOperator {
    override val argc = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): List<SpellDatum<*>> {
        val target = args.getChecked<ItemEntity>(0)
        val stack = target.item
        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(stack)
            ?: throw MishapBadItem.of(target, "iota.read")

        ctx.assertEntityInRange(target)


        val datum = datumHolder.readDatum(ctx.world)
            ?: datumHolder.emptyDatum()
            ?: throw MishapBadItem.of(target, "iota.read")
        return listOf(datum)
    }
}
