package solutions.lykos.willhaben.parser.backend.importer.actions.resolvers

import solutions.lykos.willhaben.parser.backend.importer.HashMapping
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node

abstract class HashResolver<T : Node>() : Resolver<T, HashMapping>()
