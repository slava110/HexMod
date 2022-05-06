package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.utils.ManaHelper
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.entity.item.ItemEntity

class OpMakePackagedSpell<T : ItemPackagedHex>(val itemType: T, val cost: Int) : SpellOperator {
    override val argc = 2
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val entity = args.getChecked<ItemEntity>(0)
        val patterns = args.getChecked<List<SpellDatum<*>>>(1).map {
            if (it.payload is HexPattern)
                it.payload
            else
                throw MishapInvalidIota(it, 0, TranslatableComponent("hexcasting.mishap.invalid_value.list.pattern"))
        }

        val (handStack, hand) = ctx.getHeldItemToOperateOn { it.`is`(itemType) }
        if (!handStack.`is`(itemType)) {
            throw MishapBadOffhandItem(handStack, hand, itemType.description)
        }

        ctx.assertEntityInRange(entity)
        if (!ManaHelper.isManaItem(entity.item) || ManaHelper.extractMana(
                entity.item,
                drainForBatteries = true,
                simulate = true
            ) <= 0
        ) {
            throw MishapBadItem.of(
                entity,
                "mana"
            )
        }

        return Triple(Spell(entity, patterns), cost, listOf(ParticleSpray.Burst(entity.position(), 0.5)))
    }

    private inner class Spell(val itemEntity: ItemEntity, val patterns: List<HexPattern>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn { it.`is`(itemType) }
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
            if (hexHolder != null
                && hexHolder.patterns == null
                && itemEntity.isAlive
            ) {
                val entityStack = itemEntity.item.copy()
                val manaAmt = ManaHelper.extractMana(entityStack, drainForBatteries = true)
                if (manaAmt > 0) {
                    hexHolder.writePatterns(patterns, manaAmt)
                }

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
